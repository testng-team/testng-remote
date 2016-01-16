package org.testng.remote;

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.testng.remote.support.RemoteTestNGFactory;

public abstract class AbstractRemoteTestNGFactory implements RemoteTestNGFactory {

  @Override
  public boolean accept(Version version) {
    return version == null || getAcceptableVersions().includes(version);
  }

  protected abstract VersionRange getAcceptableVersions();
}
