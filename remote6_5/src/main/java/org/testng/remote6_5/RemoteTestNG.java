package org.testng.remote6_5;

import com.google.auto.service.AutoService;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                          listenerCollectionToArray(listeners));
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
      TestRunner tr = m_delegateFactory.newTestRunner(suite, test, listenerCollectionToArray(listeners));
      tr.addListener(new RemoteTestListener(suite, test, m_messageSender));
      return tr;
    }
  }

  private static List<IInvokedMethodListener> listenerCollectionToArray(Collection<IInvokedMethodListener> listeners) {
    // Convert Collection to Set to make sure only register one instance for invoked method listener 
    Set<IInvokedMethodListener> invokedMethodListenerSet = new HashSet<>();
    for (IInvokedMethodListener listener : listeners) {
      invokedMethodListenerSet.add(listener);
    }

    // Convert the Set to List is to be back-compatible with 6.8.x or below
    List<IInvokedMethodListener> invokedMethodListeners = new ArrayList<>();
    for (IInvokedMethodListener listener : invokedMethodListenerSet) {
      invokedMethodListeners.add(listener);
    }

    return invokedMethodListeners;
  }
}
