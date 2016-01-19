package test.remote;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.remote.RemoteArgs;
import org.testng.remote.RemoteTestNG;
import org.testng.remote.strprotocol.IMessage;
import org.testng.remote.strprotocol.IMessageSender;
import org.testng.remote.strprotocol.JsonMessageSender;
import org.testng.remote.strprotocol.MessageHub;
import org.testng.remote.strprotocol.SerializedMessageSender;
import org.testng.remote.strprotocol.StringMessageSender;

import test.SimpleBaseTest;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple client that launches RemoteTestNG and then talks to it via the
 * two supported protocols, String and Serialized.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public abstract class RemoteTest extends SimpleBaseTest {

  private static final boolean DEBUG = true;

  protected abstract String getTestNGVersion();

  // Note: don't use the ports used by the plug-in or the RemoteTestNG processes
  // launched in this test will interfere with the plug-in.
  private static final int PORT1 = 1243;
  private static final int PORT2 = 1242;
  private static final int PORT3 = 1244;
  private static final List<String> EXPECTED_MESSAGES = new ArrayList<String>() {{
    add("GenericMessage"); // method and test counts
    add("SuiteMessage");  // suite started
    add("TestMessage");  // test started
    add("TestResultMessage"); // status: started
    add("TestResultMessage"); // status: success
    add("TestResultMessage"); // status: started
    add("TestResultMessage"); // status: success
    add("TestMessage"); // test finished
    add("SuiteMessage"); // suite finished
  }};

  @Test
  public void testSerialized() {
    runTest("-serport", PORT1, new SerializedMessageSender("localhost", PORT1));
  }

  @Test
  public void testJson() {
    runTest("-serport", PORT3, new JsonMessageSender("localhost", PORT3));
  }

  @Test
  public void testString() {
    runTest("-port", PORT2, new StringMessageSender("localhost", PORT2));
  }

  private void launchRemoteTestNG(final String portArg, final int portValue, final String protocol) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        List<String> args = new ArrayList<>();
        args.add(portArg);
        args.add(Integer.toString(portValue));
        args.add(RemoteArgs.VERSION);
        args.add(getTestNGVersion());
        if (protocol != null) {
          args.add(RemoteArgs.PROTOCOL);
          args.add(protocol);
        }
        args.add("-dontexit");
        args.add(getPathToResource("testng-remote.xml"));
        RemoteTestNG.main(args.toArray(new String[0]));
        }
      }).start();
  }

  private void runTest(String arg, int portValue, IMessageSender sms) {
    p("Launching RemoteTestNG on port " + portValue);
    String protocol = null;
    if (sms instanceof JsonMessageSender) {
      protocol = "json";
    }
    launchRemoteTestNG(arg, portValue, protocol);
    MessageHub mh = new MessageHub(sms);
    List<String> received = Lists.newArrayList();
    try {
      mh.initReceiver();
      IMessage message = mh.receiveMessage();
      while (message != null) {
        received.add(message.getClass().getSimpleName());
        message = mh.receiveMessage();
      }

      Assert.assertEquals(received, EXPECTED_MESSAGES);
    }
    catch(SocketTimeoutException ex) {
      Assert.fail("Time out");
    }
  }

  private static void p(String s) {
    if (DEBUG) {
      System.out.println("[RemoteTest] " + s);
    }
  }
}
