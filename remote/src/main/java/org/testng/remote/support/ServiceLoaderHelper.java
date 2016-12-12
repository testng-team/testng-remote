package org.testng.remote.support;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.testng.TestNGException;
import org.testng.remote.RemoteTestNG;
import org.testng.shaded.osgi.framework.Version;

public final class ServiceLoaderHelper {

    private ServiceLoaderHelper() {}

    public static RemoteTestNGFactory getFirst(Version version) {
        List<RemoteTestNGFactory> factories = new ArrayList<>();
        for (RemoteTestNGFactory factory : ServiceLoader.load(RemoteTestNGFactory.class)) {
            if (factory.accept(version)) {
                factories.add(factory);
            }
        }
        if (factories.isEmpty()) {
            throw new TestNGException(version + " is not a supported TestNG version");
        }
        if (factories.size() > 1) {
            System.err.println("[ServiceLoaderHelper] More than one working implementation for '" + version + "', we will use the first one");
        }

        Collections.sort(factories, new Comparator<RemoteTestNGFactory>() {
          @Override
          public int compare(RemoteTestNGFactory o1, RemoteTestNGFactory o2) {
            // the newest first
            return o2.getOrder() - o1.getOrder();
          }
        });

        return factories.get(0);
    }

  public static RemoteTestNGFactory getFirstQuietly(String version) {
    return getFirstQuietly(Version.parseVersion(version));
  }

  /**
   * Get the first RemoteTestNGFactory on classpath.
   * <p>
   * this implementation is diff with {@link #getFirst(Version)} that 
   * it scans the JARs on the classpath, and parse the services file manually.
   * </p>
   * @param version
   * @return
   * @throws TestNGException if not found
   */
  public static RemoteTestNGFactory getFirstQuietly(Version version) {
    ClassLoader cl = RemoteTestNGFactory.class.getClassLoader();
    if (cl instanceof URLClassLoader) {
      for (URL url : ((URLClassLoader) cl).getURLs()) {
        File f = new File(url.getFile());
        // only check for jar file name starts with 'testng-remote'
        if (f.isFile() && f.getName().startsWith("testng-remote")) {
          try (JarFile jarFile = new JarFile(f)) {
            JarEntry entry = jarFile.getJarEntry("META-INF/services/" + RemoteTestNGFactory.class.getName());
            if (entry != null) {
              ArrayList<String> names = new ArrayList<>();

              try (BufferedReader r = new BufferedReader(
                  new InputStreamReader(jarFile.getInputStream(entry), "utf-8"))) {
                int lc = 1;
                while ((lc = parseLine(RemoteTestNGFactory.class, url, r, lc, names)) >= 0) {
                  // noop
                }

                for (String name : names) {
                  Class<?> c = Class.forName(name, false, cl);
                  RemoteTestNGFactory factory = (RemoteTestNGFactory) c.newInstance();
                  if (factory.accept(version)) {
                    return factory;
                  }
                }
              }
            }
          } catch (TestNGException ex) {
            throw ex;
          } catch (Exception ex) {
            if (RemoteTestNG.isDebug()) {
              ex.printStackTrace();
            }
          }
        }
      }
    }

    throw new TestNGException(version + " is not a supported TestNG version");
  }

  // Parse a single line from the given configuration file, adding the name
  // on the line to the names list.
  //
  private static int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names)
      throws IOException, ServiceConfigurationError {
    String ln = r.readLine();
    if (ln == null) {
      return -1;
    }
    int ci = ln.indexOf('#');
    if (ci >= 0) {
      ln = ln.substring(0, ci);
    }
    ln = ln.trim();
    int n = ln.length();
    if (n != 0) {
      if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
        fail(service, u, lc, "Illegal configuration-file syntax");
      }
      int cp = ln.codePointAt(0);
      if (!Character.isJavaIdentifierStart(cp)) {
        fail(service, u, lc, "Illegal provider-class name: " + ln);
      }
      for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
        cp = ln.codePointAt(i);
        if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
          fail(service, u, lc, "Illegal provider-class name: " + ln);
        }
      }
      if (!names.contains(ln)) {
        names.add(ln);
      }
    }
    return lc + 1;
  }

  private static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
    throw new ServiceConfigurationError(service.getName() + ": " + msg);
  }

  private static void fail(Class<?> service, URL u, int line, String msg) throws ServiceConfigurationError {
    fail(service, u + ":" + line + ": " + msg);
  }
}
