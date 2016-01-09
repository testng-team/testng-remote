package org.testng.remote.support;

import com.google.auto.service.AutoService;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.testng.remote.IRemoteTestNG;

@AutoService(RemoteTestNGFactory.class)
public class RemoteTestNGFactory6_9_7 implements RemoteTestNGFactory {

    private static final VersionRange RANGE = new VersionRange("[6.9.7,6.9.10)");

    @Override
    public boolean accept(Version version) {
        return version != null && RANGE.includes(version);
    }

    @Override
    public SuiteDispatcherAdapter createSuiteDispatcherAdapter() {
        return new SuiteDispatcherAdapter6_9_7();
    }

    @Override
    public IRemoteTestNG createRemoteTestNG() {
        return new RemoteTestNG6_9_7();
    }
}
