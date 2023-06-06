package org.testng.remote.support;

import org.testng.remote.AbstractRemoteTestNGFactory;
import org.testng.remote.IRemoteTestNG;
import org.testng.shaded.osgi.framework.VersionRange;

import com.google.auto.service.AutoService;

@AutoService(RemoteTestNGFactory.class)
public class RemoteTestNGFactory6_5 extends AbstractRemoteTestNGFactory {

  private static final VersionRange RANGE = new VersionRange("[6.5.1,6.9.7)");

  @Override
  public IRemoteTestNG createRemoteTestNG() {
    return new RemoteTestNG6_5();
  }

  @Override
  protected VersionRange getAcceptableVersions() {
    return RANGE;
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
