package org.testng.remote;

import org.testng.shaded.osgi.framework.Version;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

public class RemoteArgs {
  public static final String PORT = "-serport";
  @Parameter(names = PORT, description = "The port for the serialization protocol")
  public Integer serPort;

  public static final String PROTOCOL = "-protocol";
  @Parameter(names = PROTOCOL, description = "The protocol for message inter-communication")
  public String protocol;

  public static final String DONT_EXIT= "-dontexit";
  @Parameter(names = DONT_EXIT, description = "Do not exit the JVM once done")
  public boolean dontExit = false;

  public static final String ACK = "-ack";
  @Parameter(names = ACK, description = "Use ACK's")
  public boolean ack = false;

  public static final String VERSION = "-version";
  @Parameter(names = VERSION, description = "TestNG target version", converter = VersionConverter.class)
  public Version version;

  public static class VersionConverter implements IStringConverter<Version> {

    @Override
    public Version convert(String value) {
      if (value == null) {
        return null;
      }

      return RemoteTestNG.toVersion(value);
    }
  }
}
