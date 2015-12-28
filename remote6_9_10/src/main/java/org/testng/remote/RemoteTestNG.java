package org.testng.remote;

import com.beust.jcommander.ParameterException;
import org.testng.*;
import org.testng.remote.strprotocol.MessageHub;
import org.testng.remote.strprotocol.RemoteTestListener;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.List;

public class RemoteTestNG extends AbstractRemoteTestNG {

  @Override
  protected ITestRunnerFactory buildTestRunnerFactory() {
    if(null == m_customTestRunnerFactory) {
      m_customTestRunnerFactory= new ITestRunnerFactory() {
          @Override
          public TestRunner newTestRunner(ISuite suite, XmlTest xmlTest,
              Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
            TestRunner runner =
              new TestRunner(getConfiguration(), suite, xmlTest,
                  false /*skipFailedInvocationCounts */,
                  listeners, classListeners);
            if (m_useDefaultListeners) {
              runner.addListener(new TestHTMLReporter());
              runner.addListener(new JUnitXMLReporter());
            }

            return runner;
          }
        };
    }

    return m_customTestRunnerFactory;
  }

  public static void main(String[] args) throws ParameterException {
    RemoteTestNG remoteTestNg = new RemoteTestNG();
    AbstractRemoteTestNG.main(remoteTestNg, args);
  }

  @Override
  protected ITestRunnerFactory createDelegatingTestRunnerFactory(ITestRunnerFactory trf, MessageHub smsh) {
    return new DelegatingTestRunnerFactory(trf, smsh);
  }

  private static class DelegatingTestRunnerFactory implements ITestRunnerFactory {
    private final ITestRunnerFactory m_delegateFactory;
    private final MessageHub m_messageSender;

    DelegatingTestRunnerFactory(ITestRunnerFactory trf, MessageHub smsh) {
      m_delegateFactory= trf;
      m_messageSender= smsh;
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
      TestRunner tr = m_delegateFactory.newTestRunner(suite, test, listeners, classListeners);
      tr.addListener(new RemoteTestListener(suite, test, m_messageSender));
      return tr;
    }
  }
}
