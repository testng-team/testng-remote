package org.testng.remote;

import org.testng.CommandLineArgs;

public interface IRemoteTestNG {

    void dontExit(boolean dontExit);
    void setDebug(boolean debug);
    void setAck(boolean ack);
    void configure(CommandLineArgs cla);
    void setHost(String host);
    void setSerPort(Integer serPort);
    void setProtocol(String protocol);
    void setPort(Integer port);
    void run();
}
