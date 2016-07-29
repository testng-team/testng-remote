package org.testng.remote.support;

import org.osgi.framework.Version;
import org.testng.TestNGException;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

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
        return factories.get(0);
    }

    /**
     * Get the first RemoteTestNGFactory.
     * <p>
     * this is specially for issue #29, with assumption that testng-remote jar is on front of any jar contains INDEX.LIST file.
     * if the first RemoteTestNGFactory found, just return it.
     * </p>
     * @param version
     * @return the RemoteTestNGFactory instance
     */
    public static RemoteTestNGFactory getFirstQuietly(Version version) {
      try {
        for (RemoteTestNGFactory factory : ServiceLoader.load(RemoteTestNGFactory.class)) {
          if (factory.accept(version)) {
              return factory;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      throw new TestNGException(version + " is not a supported TestNG version");
  }
}
