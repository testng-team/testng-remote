package org.testng.remote;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.testng.CommandLineArgs;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.remote.strprotocol.*;
import org.testng.xml.XmlSuite;

import java.util.Arrays;
import java.util.List;

import static org.testng.internal.Utils.defaultIfStringEmpty;

/**
 * Extension of TestNG registering a remote TestListener.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public abstract class AbstractRemoteTestNG extends TestNG {
  private static final String LOCALHOST = "localhost";

  // The following constants are referenced by the Eclipse plug-in, make sure you
  // modify the plug-in as well if you change any of them.
  public static final String DEBUG_PORT = "12345";
  public static final String DEBUG_SUITE_FILE = "testng-customsuite.xml";
  public static final String DEBUG_SUITE_DIRECTORY = System.getProperty("java.io.tmpdir");
  public static final String PROPERTY_DEBUG = "testng.eclipse.debug";
  public static final String PROPERTY_VERBOSE = "testng.eclipse.verbose";
  // End of Eclipse constants.

  protected ITestRunnerFactory m_customTestRunnerFactory;
  private String m_host;

  /** Port used for the string protocol */
  private Integer m_port = null;

  /** Port used for the serialized protocol */
  private static Integer m_serPort = null;

  /** Protocol used for inter-communication */
  private static String m_protocol;

  private static boolean m_debug;

  private static boolean m_dontExit;

  private static boolean m_ack;

  public void setHost(String host) {
    m_host = defaultIfStringEmpty(host, LOCALHOST);
  }

  private void calculateAllSuites(List<XmlSuite> suites, List<XmlSuite> outSuites) {
    for (XmlSuite s : suites) {
      outSuites.add(s);
//      calculateAllSuites(s.getChildSuites(), outSuites);
    }
  }

  @Override
  public void run() {
    IMessageSender sender = getMessageSender();
    final MessageHub msh = new MessageHub(sender);
    msh.setDebug(isDebug());
    try {
      msh.connect();
      // We couldn't do this until now in debug mode since the .xml file didn't exist yet.
      // Now that we have connected with the Eclipse client, we know that it created the .xml
      // file so we can proceed with the initialization
      initializeSuitesAndJarFile();

      List<XmlSuite> suites = Lists.newArrayList();
      calculateAllSuites(m_suites, suites);
//      System.out.println("Suites: " + m_suites.get(0).getChildSuites().size()
//          + " and:" + suites.get(0).getChildSuites().size());
      if(suites.size() > 0) {

        int testCount= 0;

        for (XmlSuite suite : suites) {
          testCount += suite.getTests().size();
        }

        GenericMessage gm= new GenericMessage(MessageHelper.GENERIC_SUITE_COUNT);
        gm.setSuiteCount(suites.size());
        gm.setTestCount(testCount);
        msh.sendMessage(gm);

        addListener(new RemoteSuiteListener(msh));
        setTestRunnerFactory(createDelegatingTestRunnerFactory(buildTestRunnerFactory(), msh));

//        System.out.println("RemoteTestNG starting");
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

  private IMessageSender getMessageSender() {
    if (m_protocol != null) {
      switch (m_protocol) {
      case "object":
        return new SerializedMessageSender(m_host, m_serPort, m_ack);
      case "string":
        return new StringMessageSender(m_host, m_port);
      case "json":
        return new JsonMessageSender(m_host, m_serPort, m_ack);
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

  protected static void main(AbstractRemoteTestNG remoteTestNg, String[] args) throws ParameterException {
    CommandLineArgs cla = new CommandLineArgs();
    RemoteArgs ra = new RemoteArgs();
    new JCommander(Arrays.asList(cla, ra), args);
    m_dontExit = ra.dontExit;
    if (cla.port != null && ra.serPort != null) {
      throw new TestNGException("Can only specify one of " + CommandLineArgs.PORT
          + " and " + RemoteArgs.PORT);
    }
    m_debug = cla.debug;
    m_ack = ra.ack;
    if (m_debug) {
//      while (true) {
        initAndRun(remoteTestNg, args, cla, ra);
//      }
    }
    else {
      initAndRun(remoteTestNg, args, cla, ra);
    }
  }

  private static void initAndRun(AbstractRemoteTestNG remoteTestNg, String[] args, CommandLineArgs cla, RemoteArgs ra) {
    if (m_debug) {
      // In debug mode, override the port and the XML file to a fixed location
      cla.port = Integer.parseInt(DEBUG_PORT);
      ra.serPort = cla.port;
      cla.suiteFiles = Arrays.asList(new String[] {
          DEBUG_SUITE_DIRECTORY + DEBUG_SUITE_FILE
      });
    }
    remoteTestNg.configure(cla);
    remoteTestNg.setHost(cla.host);
    m_serPort = ra.serPort;
    m_protocol = ra.protocol;
    remoteTestNg.m_port = cla.port;
    if (isVerbose()) {
      StringBuilder sb = new StringBuilder("Invoked with ");
      for (String s : args) {
        sb.append(s).append(" ");
      }
      p(sb.toString());
//      remoteTestNg.setVerbose(1);
//    } else {
//      remoteTestNg.setVerbose(0);
    }
    validateCommandLineParameters(cla);
    remoteTestNg.run();
//    if (m_debug) {
//      // Run in a loop if in debug mode so it is possible to run several launches
//      // without having to relauch RemoteTestNG.
//      while (true) {
//        remoteTestNg.run();
//        remoteTestNg.configure(cla);
//      }
//    } else {
//      remoteTestNg.run();
//    }
  }

  private static void p(String s) {
    if (isVerbose()) {
      System.out.println("[RemoteTestNG] " + s);
    }
  }

  public static boolean isVerbose() {
    boolean result = System.getProperty(PROPERTY_VERBOSE) != null || isDebug();
    return result;
  }

  public static boolean isDebug() {
    return m_debug || System.getProperty(PROPERTY_DEBUG) != null;
  }

  private String getHost() {
    return m_host;
  }

  private int getPort() {
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
