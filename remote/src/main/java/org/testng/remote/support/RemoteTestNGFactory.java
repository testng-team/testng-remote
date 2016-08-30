package org.testng.remote.support;

import org.osgi.framework.Version;
import org.testng.remote.IRemoteTestNG;
import org.testng.remote.Orderable;

public interface RemoteTestNGFactory extends Orderable {

    boolean accept(Version version);
    IRemoteTestNG createRemoteTestNG();
}
