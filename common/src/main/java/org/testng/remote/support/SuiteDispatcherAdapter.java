package org.testng.remote.support;

import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public interface SuiteDispatcherAdapter {

    XmlSuite copy(XmlSuite suite, XmlTest test);
    XmlTest copy(XmlTest test, XmlSuite suite);
}
