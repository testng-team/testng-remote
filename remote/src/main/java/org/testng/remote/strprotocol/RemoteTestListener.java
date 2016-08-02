package org.testng.remote.strprotocol;

import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;
import org.testng.xml.XmlTest;

/**
 * A special listener that remote the event with string protocol.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class RemoteTestListener extends RemoteTestListener1 implements IResultListener2 {

  public RemoteTestListener(ISuite suite, XmlTest test, MessageHub msh) {
    super(suite, test, msh);
  }

  @Override
  public void beforeConfiguration(ITestResult tr) {
  }

}