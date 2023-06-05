package org.testng.remote.support;

import org.testng.remote.AbstractRemoteTestNGFactory;
import org.testng.remote.IRemoteTestNG;
import org.testng.shaded.osgi.framework.VersionRange;

import com.google.auto.service.AutoService;

@AutoService(RemoteTestNGFactory.class)
public class RemoteTestNGFactory7_8 extends AbstractRemoteTestNGFactory {

  private static final VersionRange RANGE = new VersionRange("7.8");

  @Override
  public IRemoteTestNG createRemoteTestNG() {
    return new RemoteTestNG7_8();
  }

  @Override
  protected VersionRange getAcceptableVersions() {
    return RANGE;
  }

  @Override
  public int getOrder() {
    return 7;
  }
}
