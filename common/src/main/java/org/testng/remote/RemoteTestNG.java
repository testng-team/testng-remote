package org.testng.remote;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.testng.CommandLineArgs;
import org.testng.TestNGException;
import org.testng.remote.support.ServiceLoaderHelper;

import java.util.*;

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

        IRemoteTestNG remoteTestNg = ServiceLoaderHelper.getFirst(ra.version).createRemoteTestNG();
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

    public static boolean isVerbose() {
        boolean result = System.getProperty(PROPERTY_VERBOSE) != null || isDebug();
        return result;
    }

    public static boolean isDebug() {
        return m_debug || System.getProperty(PROPERTY_DEBUG) != null;
    }
}
