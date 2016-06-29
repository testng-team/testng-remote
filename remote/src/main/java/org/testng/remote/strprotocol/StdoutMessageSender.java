package org.testng.remote.strprotocol;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * this is a dummy IMessageSender implementation for test purpose only.
 * 
 * @author nick
 */
public class StdoutMessageSender implements IMessageSender {

  @Override
  public void connect() throws IOException {
    // noop
  }

  @Override
  public void initReceiver() throws SocketTimeoutException {
    // noop
  }

  @Override
  public void stopReceiver() {
    // noop
  }

  @Override
  public void sendMessage(IMessage message) throws Exception {
    System.out.println(message);
  }

  @Override
  public IMessage receiveMessage() throws Exception {
    return null;
  }

  @Override
  public void shutDown() {
    // noop
  }

  @Override
  public void sendAck() {
    // noop
  }

  @Override
  public void sendStop() {
    // noop
  }

}
