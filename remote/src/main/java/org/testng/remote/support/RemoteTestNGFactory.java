package org.testng.remote.support;

import org.testng.remote.IRemoteTestNG;
import org.testng.remote.Orderable;
import org.testng.shaded.osgi.framework.Version;

public interface RemoteTestNGFactory extends Orderable {

    boolean accept(Version version);
    IRemoteTestNG createRemoteTestNG();
}
