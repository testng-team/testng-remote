package org.testng.remote;

import org.testng.TestNGException;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class SuiteDispatcher extends AbstractSuiteDispatcher {

	public SuiteDispatcher(String propertiesFile) throws TestNGException {
		super(propertiesFile);
	}

	@Override
	protected XmlSuite copy(XmlSuite suite, XmlTest test) {
		XmlSuite tmpSuite = new XmlSuite();
		tmpSuite.setXmlPackages(suite.getXmlPackages());
		tmpSuite.setJUnit(suite.isJUnit());
		tmpSuite.setSkipFailedInvocationCounts(suite.skipFailedInvocationCounts());
		tmpSuite.setName("Temporary suite for " + test.getName());
		tmpSuite.setParallel(suite.getParallel());
		tmpSuite.setParameters(suite.getParameters());
		tmpSuite.setThreadCount(suite.getThreadCount());
		tmpSuite.setDataProviderThreadCount(suite.getDataProviderThreadCount());
		tmpSuite.setVerbose(suite.getVerbose());
		tmpSuite.setObjectFactory(suite.getObjectFactory());
		return tmpSuite;
	}

	@Override
	protected XmlTest copy(XmlTest test, XmlSuite suite) {
		XmlTest tmpTest = new XmlTest(suite);
		tmpTest.setBeanShellExpression(test.getExpression());
		tmpTest.setXmlClasses(test.getXmlClasses());
		tmpTest.setExcludedGroups(test.getExcludedGroups());
		tmpTest.setIncludedGroups(test.getIncludedGroups());
		tmpTest.setJUnit(test.isJUnit());
		tmpTest.setMethodSelectors(test.getMethodSelectors());
		tmpTest.setName(test.getName());
		tmpTest.setParallel(test.getParallel());
		tmpTest.setParameters(test.getTestParameters());
		tmpTest.setVerbose(test.getVerbose());
		tmpTest.setXmlClasses(test.getXmlClasses());
		tmpTest.setXmlPackages(test.getXmlPackages());
		return tmpTest;
	}
}
