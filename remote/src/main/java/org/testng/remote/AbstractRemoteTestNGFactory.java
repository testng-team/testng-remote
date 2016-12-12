package org.testng.remote;

import org.testng.remote.support.RemoteTestNGFactory;
import org.testng.shaded.osgi.framework.Version;
import org.testng.shaded.osgi.framework.VersionRange;

public abstract class AbstractRemoteTestNGFactory implements RemoteTestNGFactory {

  @Override
  public boolean accept(Version version) {
    return version == null || getAcceptableVersions().includes(version);
  }

  protected abstract VersionRange getAcceptableVersions();
}
