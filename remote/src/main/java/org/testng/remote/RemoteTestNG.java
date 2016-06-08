package org.testng.remote;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.osgi.framework.Version;
import org.testng.CommandLineArgs;
import org.testng.TestNGException;
import org.testng.remote.support.ServiceLoaderHelper;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class RemoteTestNG {

    // The following constants are referenced by the Eclipse plug-in, make sure you
    // modify the plug-in as well if you change any of them.
    public static final String DEBUG_PORT = "12345";
    public static final String DEBUG_SUITE_FILE = "testng-customsuite.xml";
    public static final String DEBUG_SUITE_DIRECTORY = System.getProperty("java.io.tmpdir");
    public static final String PROPERTY_DEBUG = "testng.eclipse.debug";
    public static final String PROPERTY_VERBOSE = "testng.eclipse.verbose";
    // End of Eclipse constants.

    private static boolean m_debug;

    public static void main(String[] args) throws ParameterException {
        CommandLineArgs cla = new CommandLineArgs();
        RemoteArgs ra = new RemoteArgs();
        new JCommander(Arrays.asList(cla, ra), args);

        Version ver = ra.version;
        if (ver == null) {
          // no version specified on cli, detect ourself
          ver = getTestNGVersion();
        }

        p("detected TestNG version " + ver);
        IRemoteTestNG remoteTestNg = ServiceLoaderHelper.getFirst(ver).createRemoteTestNG();
        remoteTestNg.dontExit(ra.dontExit);
        if (cla.port != null && ra.serPort != null) {
            throw new TestNGException("Can only specify one of " + CommandLineArgs.PORT
                    + " and " + RemoteArgs.PORT);
        }
        m_debug = cla.debug;
        remoteTestNg.setDebug(cla.debug);
        remoteTestNg.setAck(ra.ack);
        if (m_debug) {
//      while (true) {
            initAndRun(remoteTestNg, args, cla, ra);
//      }
        }
        else {
            initAndRun(remoteTestNg, args, cla, ra);
        }
    }

    private static Version getTestNGVersion() {
      String errMsg = "";
      try {
        // use reflection to read org.testng.internal.Version.VERSION for reason of:
        // 1. bypass the javac compile time constant substitution
        // 2. org.testng.internal.Version is available since version 6.6
        @SuppressWarnings("rawtypes")
        Class clazz = Class.forName("org.testng.internal.Version");
        Field field = clazz.getDeclaredField("VERSION");
        String strVer = (String) field.get(null);

        // trim the version to leave digital number only
        int idx = strVer.indexOf("beta");
        if (idx > 0) {
          strVer = strVer.substring(0, idx);
        }
        return new Version(strVer);
      } catch (Exception e) {
        // testng version < 6.6, ClassNotFound: org.testng.internal.Version
        // parse the MANIFEST.MF of testng jar from classpath
        try {
          Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources("META-INF/MANIFEST.MF");
          while (resources.hasMoreElements()) {
            Manifest mf = new Manifest(resources.nextElement().openStream());
            Attributes mainAttrs = mf.getMainAttributes();
            if ("org.testng.TestNG".equals(mainAttrs.getValue("Main-Class"))) {
              return new Version(mainAttrs.getValue("Implementation-Version"));
            }

            if ("org.testng".equals(mainAttrs.getValue("Bundle-SymbolicName"))) {
              return new Version(mainAttrs.getValue("Bundle-Version"));
            }

            if ("testng".equals(mainAttrs.getValue("Specification-Title"))) {
              return new Version(mainAttrs.getValue("Specification-Version"));
            }
          }
        } catch (Exception ex) {
          errMsg = ex.getMessage();
          if (isDebug()) {
            ex.printStackTrace();
          }
        }
      }

      p("No TestNG version found on classpath");
      ClassLoader cl = ClassLoader.getSystemClassLoader();
      if (cl instanceof URLClassLoader) {
        p(join(((URLClassLoader) cl).getURLs(), ", "));
      }

      throw new RuntimeException("Can't recognize the TestNG version on classpath. " + errMsg);
    }

    private static void initAndRun(IRemoteTestNG remoteTestNg, String[] args, CommandLineArgs cla, RemoteArgs ra) {
        if (m_debug) {
            // In debug mode, override the port and the XML file to a fixed location
            cla.port = Integer.parseInt(DEBUG_PORT);
            ra.serPort = cla.port;
            cla.suiteFiles = Arrays.asList(new String[] {
                    DEBUG_SUITE_DIRECTORY + DEBUG_SUITE_FILE
            });
        }
        remoteTestNg.configure(cla);
        remoteTestNg.setHost(cla.host);
        remoteTestNg.setSerPort(ra.serPort);
        remoteTestNg.setProtocol(ra.protocol);
        remoteTestNg.setPort(cla.port);
        if (isVerbose()) {
            StringBuilder sb = new StringBuilder("Invoked with ");
            for (String s : args) {
                sb.append(s).append(" ");
            }
            p(sb.toString());
//      remoteTestNg.setVerbose(1);
//    } else {
//      remoteTestNg.setVerbose(0);
        }
        AbstractRemoteTestNG.validateCommandLineParameters(cla);
        remoteTestNg.run();
//    if (m_debug) {
//      // Run in a loop if in debug mode so it is possible to run several launches
//      // without having to relauch RemoteTestNG.
//      while (true) {
//        remoteTestNg.run();
//        remoteTestNg.configure(cla);
//      }
//    } else {
//      remoteTestNg.run();
//    }
    }

    private static void p(String s) {
        if (isVerbose()) {
            System.out.println("[RemoteTestNG] " + s);
        }
    }

    private static String join(URL[] urls, String sep) {
      StringBuilder sb = new StringBuilder();
      for (URL url : urls) {
        sb.append(url);
        sb.append(sep);
      }
      return sb.toString();
    }

    public static boolean isVerbose() {
        boolean result = System.getProperty(PROPERTY_VERBOSE) != null || isDebug();
        return result;
    }

    public static boolean isDebug() {
        return m_debug || System.getProperty(PROPERTY_DEBUG) != null;
    }
}
