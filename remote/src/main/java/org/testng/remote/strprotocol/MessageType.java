package org.testng.remote.strprotocol;

public enum MessageType {

  GENERIC(MessageHelper.GENERIC_SUITE_COUNT), 
  SUITE(MessageHelper.SUITE), 
  TEST(MessageHelper.TEST), 
  TEST_RESULT(MessageHelper.TEST_RESULT);

  private int type;

  MessageType(int type) {
    this.type = type;
  }

  public int getValue() {
    return type;
  }

  public static MessageType fromValue(int value) {
    for (MessageType type : MessageType.values()) {
      if (type.getValue() == value) {
        return type;
      }
    }
    return null;
  }

  public static MessageType fromValue(String value) {
    int val = Integer.parseInt(value);
    return fromValue(val);
  }

  public static MessageType fromName(String name) {
    for (MessageType type : MessageType.values()) {
      if (type.name().equals(name)) {
        return type;
      }
    }
    return null;
  }
}
