package org.testng.remote.strprotocol;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.StringWriter;

import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

public class JsonMessageTest {

  @Test
  public void jsonGenericMessage() throws Exception {
    Gson gson = new GsonBuilder().create();
    GenericMessage genMsg = new GenericMessage();
    genMsg.setSuiteCount(2);
    genMsg.setTestCount(3);

    String jsonStrSuiteMsg = gson.toJson(genMsg);
    System.out.println("unmarshall to json: ");
    System.out.println(jsonStrSuiteMsg);

    GenericMessage genMsg2 = gson.fromJson(jsonStrSuiteMsg, GenericMessage.class);
    System.out.println("marshall to object: ");
    System.out.println(genMsg2);
    assertEquals(genMsg2.getType(), MessageType.GENERIC);
  }

  @Test
  public void jsonSuiteMessage() throws Exception {
    Gson gson = new GsonBuilder().create();
    SuiteMessage suiteMsg = new SuiteMessage("testSuite", true, 0);

    String jsonStrSuiteMsg = gson.toJson(suiteMsg);
    System.out.println("unmarshall to json: ");
    System.out.println(jsonStrSuiteMsg);

    SuiteMessage suiteMsg2 = gson.fromJson(jsonStrSuiteMsg, SuiteMessage.class);
    System.out.println("marshall to object: ");
    System.out.println(suiteMsg2);
    assertEquals(suiteMsg2.getType(), MessageType.SUITE);
  }

  @Test
  public void jsonMessageStream() throws Exception {
    // dummy constructor parameters since the UT only verify message
    // marshalling/unmarshalling
    JsonMessageSender ms = new JsonMessageSender("localhost", -1);

    GenericMessage genMsg = new GenericMessage();
    genMsg.setSuiteCount(2);
    genMsg.setTestCount(3);
    StringWriter sWriter = new StringWriter();
    JsonWriter writer = new JsonWriter(sWriter);
    ms.writeMessage(writer, genMsg);
    String jsonMsg = sWriter.toString();
    System.out.println("unmarshall to json: ");
    System.out.println(jsonMsg);

    IMessage message = ms.deserializeMessage(jsonMsg);
    assertTrue(message instanceof GenericMessage);
    assertEquals(message.getType(), MessageType.GENERIC);
    System.out.println("marshall to object: ");
    System.out.println(message);
  }
}
