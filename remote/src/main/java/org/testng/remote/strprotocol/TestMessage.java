package org.testng.remote.strprotocol;

import org.testng.ITestContext;

import com.google.gson.annotations.SerializedName;


/**
 * An <code>IStringMessage</code> implementation for test events.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class TestMessage implements IStringMessage {
  private static final long serialVersionUID = -5039267143570559640L;
  @SerializedName("testStart")
  protected final boolean m_testStart;
  @SerializedName("suiteName")
  protected final String m_suiteName;
  @SerializedName("testName")
  protected final String m_testName;
  @SerializedName("testMethodCount")
  protected final int m_testMethodCount;
  @SerializedName("passedTestCount")
  protected final int m_passedTestCount;
  @SerializedName("failedTestCount")
  protected final int m_failedTestCount;
  @SerializedName("skippedTestCount")
  protected final int m_skippedTestCount;
  @SerializedName("successPercentageFailedTestCount")
  protected final int m_successPercentageFailedTestCount;

  public TestMessage(final boolean isTestStart,
              final String suiteName,
              final String testName,
              final int methodCount,
              final int passedCount,
              final int failedCount,
              final int skippedCount,
              final int percentageCount) {
    m_testStart = isTestStart;
    m_suiteName = suiteName;
    m_testName = testName;
    m_testMethodCount = methodCount;
    m_passedTestCount = passedCount;
    m_failedTestCount = failedCount;
    m_skippedTestCount = skippedCount;
    m_successPercentageFailedTestCount = percentageCount;
  }

  public TestMessage(final ITestContext testContext, final boolean isTestStart) {
    this(isTestStart,
         testContext.getSuite().getName(),
         testContext.getCurrentXmlTest().getName(),
         testContext.getAllTestMethods().length,
         testContext.getPassedTests().size(),
         testContext.getFailedTests().size(),
         testContext.getSkippedTests().size(),
         testContext.getFailedButWithinSuccessPercentageTests().size());
  }

  public boolean isMessageOnStart() {
    return m_testStart;
  }

  @Override
  public String getMessageAsString() {
    StringBuffer buf = new StringBuffer();

    buf.append(m_testStart ? MessageHelper.TEST_START : MessageHelper.TEST_FINISH)
        .append(MessageHelper.DELIMITER)
        .append(m_suiteName)
        .append(MessageHelper.DELIMITER)
        .append(m_testName)
        .append(MessageHelper.DELIMITER)
        .append(m_testMethodCount)
        .append(MessageHelper.DELIMITER)
        .append(m_passedTestCount)
        .append(MessageHelper.DELIMITER)
        .append(m_failedTestCount)
        .append(MessageHelper.DELIMITER)
        .append(m_skippedTestCount)
        .append(MessageHelper.DELIMITER)
        .append(m_successPercentageFailedTestCount)
        ;

    return buf.toString();
  }

  public String getSuiteName() {
    return m_suiteName;
  }

  public String getTestName() {
    return m_testName;
  }

  public boolean isTestStart() {
    return m_testStart;
  }
  public int getTestMethodCount() {
    return m_testMethodCount;
  }
  public int getSuccessPercentageFailedTestCount() {
    return m_successPercentageFailedTestCount;
  }
  public int getFailedTestCount() {
    return m_failedTestCount;
  }
  public int getPassedTestCount() {
    return m_passedTestCount;
  }
  public int getSkippedTestCount() {
    return m_skippedTestCount;
  }

  @Override
  public MessageType getType() {
    return MessageType.TEST;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[TestMessage ==> suite:").append(m_suiteName).append(", testName:").append(m_testName)
        .append(", passed:").append(m_passedTestCount).append(", failed:").append(m_failedTestCount)
        .append("]");
    return sb.toString();
  }

}
