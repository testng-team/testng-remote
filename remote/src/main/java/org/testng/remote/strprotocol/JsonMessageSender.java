package org.testng.remote.strprotocol;

import static org.testng.remote.RemoteTestNG.isVerbose;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class JsonMessageSender extends BaseMessageSender {

  public JsonMessageSender(String host, int port) {
    super(host, port, false);
  }

  public JsonMessageSender(String host, int port, boolean ack) {
    super(host, port, ack);
  }

  @Override
  public void sendMessage(IMessage message) throws Exception {
    if (m_outStream == null) {
      throw new IllegalStateException("Trying to send a message on a shutdown sender");
    }

    synchronized (m_outStream) {
      p("Sending message " + message);

      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(m_outStream, "UTF-8"));
      JsonWriter writer = new JsonWriter(bw);
      writeMessage(writer, message);
      bw.newLine();
      bw.flush();

      waitForAck();
    }
  }

  void writeMessage(JsonWriter writer, IMessage message) throws IOException {
    writer.beginObject();

    int messageType = getMessageType(message);
    if (messageType < 0) {
      throw new IOException("Unknown type for message: " + message);
    }
    writer.name("type").value(messageType);
    writer.name("data");
    Gson gson = new GsonBuilder().create();
    gson.toJson(message, message.getClass(), writer);

    writer.endObject();
  }

  private int getMessageType(IMessage message) {
    if (message instanceof GenericMessage) {
      return MessageHelper.GENERIC_SUITE_COUNT;
    } else if (message instanceof SuiteMessage) {
      return MessageHelper.SUITE;
    } else if (message instanceof TestMessage) {
      return MessageHelper.TEST;
    } else if (message instanceof TestResultMessage) {
      return MessageHelper.TEST_RESULT;
    }
    // unknown message type
    return -1;
  }

  @Override
  public IMessage receiveMessage() throws Exception {
    if (m_inReader == null) {
      try {
        m_inReader = new BufferedReader(new InputStreamReader(m_inStream, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        m_inReader = new BufferedReader(new InputStreamReader(m_inStream));
      }
    }
    String msg = m_inReader.readLine();
    p("received message: " + msg);

    IMessage message = null;
    if (msg != null) {
      message = deserializeMessage(msg);
    }

    try {
      sendAck();
    } catch (Exception e) {
      if (isVerbose()) {
        System.out.println("sendAck failed with error: " + e.getMessage());
        e.printStackTrace();
      }
    }
    return message;
  }

  IMessage deserializeMessage(String jsonMsg) throws IOException {
    try (JsonReader reader = new JsonReader(new StringReader(jsonMsg))) {
      reader.beginObject();

      String name = reader.nextName();
      if (!"type".equals(name)) {
        throw new IOException("type node first");
      }

      int msgType = reader.nextInt();

      name = reader.nextName();
      if (!"data".equals(name)) {
        throw new IOException("data node then");
      }

      Gson gson = new GsonBuilder().create();
      IMessage message = null;
      switch (msgType) {
      case MessageHelper.GENERIC_SUITE_COUNT:
        message = gson.fromJson(reader, GenericMessage.class);
        break;
      case MessageHelper.SUITE:
        message = gson.fromJson(reader, SuiteMessage.class);
        break;
      case MessageHelper.TEST:
        message = gson.fromJson(reader, TestMessage.class);
        break;
      case MessageHelper.TEST_RESULT:
        message = gson.fromJson(reader, TestResultMessage.class);
        break;
      default:
        throw new IOException("unknown message type: " + msgType + ", raw json: " + jsonMsg);
      }

      reader.endObject();

      return message;
    }
  }

  private static void p(String msg) {
    if (isVerbose()) {
      System.out.println("[JsonMessageSender] " + msg); //$NON-NLS-1$
    }
  }
}
