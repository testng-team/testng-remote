package org.testng.remote.strprotocol;

import com.google.gson.annotations.SerializedName;

/**
 * A generic message to be used with remote listeners.
 * It is described by a {@link #m_messageType} and can contain a <code>Map</code>
 * or values.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class GenericMessage implements IStringMessage {
  private static final long serialVersionUID = 1440074281953763545L;
//  protected Map m_properties;
  @SerializedName("messageType")
  protected final MessageType m_messageType;
  @SerializedName("suiteCount")
  private int m_suiteCount;
  @SerializedName("testCount")
  private int m_testCount;

  public GenericMessage() {
    this(MessageType.GENERIC);
  }

  public GenericMessage(final MessageType type) {
    m_messageType = type;
  }

  public int getSuiteCount() {
    return m_suiteCount;
  }

  public void setSuiteCount(int suiteCount) {
    m_suiteCount = suiteCount;
  }

  public int getTestCount() {
    return m_testCount;
  }

  public void setTestCount(int testCount) {
    m_testCount = testCount;
  }

  @Override
  public MessageType getType() {
    return m_messageType;
  }

  @Override
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();

    buf.append(getType().getValue());
    buf.append(MessageHelper.DELIMITER).append("testCount").append(getTestCount())
        .append(MessageHelper.DELIMITER).append("suiteCount").append(getSuiteCount());

    return buf.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[GenericMessage ==> suiteCount:").append(m_suiteCount).append(", testCount:").append(m_testCount).append("]");
    return sb.toString();
  }
}
