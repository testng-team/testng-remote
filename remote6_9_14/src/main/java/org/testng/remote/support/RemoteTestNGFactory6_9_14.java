package org.testng.remote.support;

import org.osgi.framework.VersionRange;
import org.testng.remote.AbstractRemoteTestNGFactory;
import org.testng.remote.IRemoteTestNG;

import com.google.auto.service.AutoService;

@AutoService(RemoteTestNGFactory.class)
public class RemoteTestNGFactory6_9_14 extends AbstractRemoteTestNGFactory {

  private static final VersionRange RANGE = new VersionRange("6.9.14");

  @Override
  public IRemoteTestNG createRemoteTestNG() {
    return new RemoteTestNG6_9_14();
  }

  @Override
  protected VersionRange getAcceptableVersions() {
    return RANGE;
  }

  @Override
  public int getOrder() {
    return 4;
  }
}
