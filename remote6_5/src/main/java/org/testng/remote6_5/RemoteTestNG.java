package org.testng.remote6_5;

import java.util.List;

import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ITestRunnerFactory;
import org.testng.TestRunner;
import org.testng.remote.AbstractRemoteTestNG;
import org.testng.remote.IRemoteTestNG;
import org.testng.remote.strprotocol.MessageHub;
import org.testng.remote.strprotocol.RemoteTestListener;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlTest;

import com.google.auto.service.AutoService;

@AutoService(IRemoteTestNG.class)
public class RemoteTestNG extends AbstractRemoteTestNG {

  @Override
  protected ITestRunnerFactory buildTestRunnerFactory() {
    if(null == m_customTestRunnerFactory) {
      m_customTestRunnerFactory= new ITestRunnerFactory() {
        @Override
        public TestRunner newTestRunner(ISuite suite, XmlTest xmlTest,
                                        List<IInvokedMethodListener> listeners) {
          TestRunner runner =
                  new TestRunner(getConfiguration(), suite, xmlTest,
                          false /*skipFailedInvocationCounts */,
                          listeners);
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
        List<IInvokedMethodListener> listeners) {
      TestRunner tr = m_delegateFactory.newTestRunner(suite, test, listeners);
      tr.addListener(new RemoteTestListener(suite, test, m_messageSender));
      return tr;
    }
  }

}
