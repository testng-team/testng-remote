package org.testng.remote.support;

import org.osgi.framework.Version;
import org.testng.remote.IRemoteTestNG;

public interface RemoteTestNGFactory {

    boolean accept(Version version);
    IRemoteTestNG createRemoteTestNG();
}
