package org.testng.xml;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.testng.TestNGException;
import org.testng.remote.strprotocol.IRemoteSuiteListener;
import org.testng.remote.strprotocol.IRemoteTestListener;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses testng-result.xml.
 *
 * @see ResultContentHandler
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class ResultXMLParser extends XMLParser<Object> {
  private IRemoteTestListener m_testListener;
  private IRemoteSuiteListener m_suiteListener;

  public ResultXMLParser(IRemoteSuiteListener suiteListener, IRemoteTestListener testListener) {
    m_suiteListener = suiteListener;
    m_testListener = testListener;
  }

  public void parse() {
  }

  @Override
  public Object parse(String currentFile, InputStream inputStream, boolean loadClasses) {
    ResultContentHandler handler = new ResultContentHandler(m_suiteListener, m_testListener,
        loadClasses);

    try {
      boolean invoked = false;
      try {
        // try with testng >= 6.8.11
        invoked = invokeParseWithReflection(this, inputStream, handler);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (!invoked) {
        // otherwise, try with testng < 6.8.11 
        Field parserField = ResultXMLParser.class.getDeclaredField("m_saxParser");
        parserField.setAccessible(true);
        Object parser = parserField.get(this);
        invokeParseWithReflection(parser, inputStream, handler);
      }

      return null;
    } catch (Exception e) {
      throw new TestNGException(e);
    }
  }

  private boolean invokeParseWithReflection(Object obj, InputStream inputStream, DefaultHandler handler) throws Exception {
    Method parseMethod = obj.getClass().getMethod("parse", InputStream.class, DefaultHandler.class);
    if (parseMethod != null) {
      parseMethod.invoke(this, inputStream, handler);
      return true;
    }
    return false;
  }
}
