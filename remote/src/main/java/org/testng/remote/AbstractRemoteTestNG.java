package org.testng.remote;

import org.testng.CommandLineArgs;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;
import org.testng.remote.strprotocol.*;
import org.testng.xml.XmlSuite;

/**
 * Extension of TestNG registering a remote TestListener.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public abstract class AbstractRemoteTestNG extends TestNG implements IRemoteTestNG {
  private static final String LOCALHOST = "localhost";

  protected ITestRunnerFactory m_customTestRunnerFactory;
  private String m_host;

  /** Port used for the string protocol */
  private Integer m_port = null;

  /** Port used for the serialized protocol */
  private Integer m_serPort = null;

  /** Protocol used for inter-communication */
  private String m_protocol;

  private boolean m_debug;

  private boolean m_dontExit;

  private boolean m_ack;

  @Override
  public void dontExit(boolean dontExit) {
    m_dontExit = dontExit;
  }

  @Override
  public void setDebug(boolean debug) {
    m_debug = debug;
  }

  @Override
  public void setAck(boolean ack) {
    m_ack = ack;
  }

  @Override
  public void setHost(String host) {
    m_host = defaultIfStringEmpty(host, LOCALHOST);
  }

  @Override
  public void setSerPort(Integer serPort) {
    m_serPort = serPort;
  }

  @Override
  public void setProtocol(String protocol) {
    m_protocol = protocol;
  }

  @Override
  public void setPort(Integer port) {
    m_port = port;
  }

  @Override
  public void configure(CommandLineArgs cla) {
    super.configure(cla);
  }

  public static void validateCommandLineParameters(CommandLineArgs args) {
    TestNG.validateCommandLineParameters(args);
  }

  public static String defaultIfStringEmpty(String s, String defaultValue) {
    return isStringEmpty(s) ? defaultValue : s;
  }

  public static boolean isStringEmpty(String s) {
    return s == null || "".equals(s);
  }

  @Override
  public void run() {
    IMessageSender sender = getMessageSender();
    final MessageHub msh = new MessageHub(sender);
    msh.setDebug(m_debug);
    try {
      msh.connect();

      initialize();

      if (canRun()) {
        int testCount = 0;

        for (XmlSuite suite : m_suites) {
          testCount += suite.getTests().size();
        }

        GenericMessage gm = new GenericMessage();
        gm.setSuiteCount(m_suites.size());
        gm.setTestCount(testCount);
        msh.sendMessage(gm);

        addListener(new RemoteSuiteListener(msh));
        setTestRunnerFactory(createDelegatingTestRunnerFactory(buildTestRunnerFactory(), msh));

        super.run();
      }
      else {
        System.err.println("No test suite found. Nothing to run");
      }
    }
    catch(Throwable cause) {
      cause.printStackTrace(System.err);
    }
    finally {
//      System.out.println("RemoteTestNG finishing: " + (getEnd() - getStart()) + " ms");
      msh.shutDown();
      if (! m_debug && ! m_dontExit) {
        System.exit(0);
      }
    }
  }

  protected void initialize() {
    // We couldn't do this until now in debug mode since the .xml file didn't exist yet.
    // Now that we have connected with the Eclipse client, we know that it created the .xml
    // file so we can proceed with the initialization
    initializeSuitesAndJarFile();
  }

  /**
   * run after {@link #initialize()}, tell if it's ready for running the test
   * 
   * @return {@code true} for ready.
   */
  protected boolean canRun() {
    return m_suites.size() > 0;
  }

  private IMessageSender getMessageSender() {
    if (m_protocol != null) {
      switch (m_protocol) {
      case "object":
        return new SerializedMessageSender(m_host, m_serPort, m_ack);
      case "string":
        return new StringMessageSender(m_host, m_port);
      case "json":
        return new JsonMessageSender(m_host, m_serPort, m_ack);
      case "stdout":
        return new StdoutMessageSender();
      default:
        throw new IllegalArgumentException("unrecognized protocol: " + m_protocol);
      }
    }

    // fall back to original behivour
    return m_serPort != null
        ? new SerializedMessageSender(m_host, m_serPort, m_ack)
        : new StringMessageSender(m_host, m_port);
  }

  /**
   * Override by the plugin if you need to configure differently the <code>TestRunner</code>
   * (usually this is needed if different listeners/reporters are needed).
   * <b>Note</b>: you don't need to worry about the wiring listener, because it is added
   * automatically.
   */
  protected abstract ITestRunnerFactory buildTestRunnerFactory();

  protected String getHost() {
    return m_host;
  }

  protected int getPort() {
    return m_port;
  }

  /** A ISuiteListener wiring the results using the internal string-based protocol. */
  private static class RemoteSuiteListener implements ISuiteListener {
    private final MessageHub m_messageSender;

    RemoteSuiteListener(MessageHub smsh) {
      m_messageSender= smsh;
    }

    @Override
    public void onFinish(ISuite suite) {
      m_messageSender.sendMessage(new SuiteMessage(suite, false /*start*/));
    }

    @Override
    public void onStart(ISuite suite) {
      m_messageSender.sendMessage(new SuiteMessage(suite, true /*start*/));
    }
  }

  protected abstract ITestRunnerFactory createDelegatingTestRunnerFactory(ITestRunnerFactory trf, MessageHub smsh);
}
