package org.testng.remote;

import org.osgi.framework.Version;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SuiteRunner;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.IConfiguration;
import org.testng.internal.Invoker;
import org.testng.internal.PropertiesFile;
import org.testng.internal.Utils;
import org.testng.remote.adapter.DefaultMastertAdapter;
import org.testng.remote.adapter.IMasterAdapter;
import org.testng.remote.adapter.RemoteResultListener;
import org.testng.remote.support.ServiceLoaderHelper;
import org.testng.remote.support.SuiteDispatcherAdapter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.*;

/**
 * Dispatches test suites according to the strategy defined.
 *
 *
 * @author	Guy Korland
 * @since 	April 20, 2007
 */
public class SuiteDispatcher
{
	/**
	 * Properties allowed in remote.properties
	 */
	public static final String MASTER_STRATEGY = "testng.master.strategy";
	public static final String VERBOSE = "testng.verbose";
	@Deprecated
	public static final String MASTER_ADPATER = "testng.master.adpter";
	public static final String MASTER_ADAPTER = "testng.master.adapter";

	/**
	 * Values allowed for STRATEGY
	 */
	public static final String STRATEGY_TEST = "test";
	public static final String STRATEGY_SUITE = "suite";

	final private int m_verbose;
	final private boolean m_isStrategyTest;

	final private IMasterAdapter m_masterAdapter;

	private final SuiteDispatcherAdapter adapter;

	/**
	 * Creates a new suite dispatcher.
	 */
	public SuiteDispatcher(String propertiesFile) throws TestNGException
	{
		try
		{
			PropertiesFile file = new PropertiesFile( propertiesFile);
			Properties properties = file.getProperties();

			m_verbose = Integer.parseInt( properties.getProperty(VERBOSE, "1"));

			String strategy = properties.getProperty(MASTER_STRATEGY, STRATEGY_SUITE);
			m_isStrategyTest = STRATEGY_TEST.equalsIgnoreCase(strategy);

			String masterAdapter = properties.getProperty(MASTER_ADAPTER);
			if (masterAdapter == null) {
				// trying deprecated value
                masterAdapter = properties.getProperty(MASTER_ADPATER);
				if (masterAdapter != null) {
					Utils.log("AbstractSuiteDispatcher", 1, "[WARN] '" + MASTER_ADPATER + "' is deprecated, use '" + MASTER_ADAPTER + "' instead.");
				}
			}
			if (masterAdapter == null) {
				m_masterAdapter = new DefaultMastertAdapter();
			} else {
				Class clazz = Class.forName(masterAdapter);
				m_masterAdapter = (IMasterAdapter)clazz.newInstance();
			}
			m_masterAdapter.init(properties);

            String version = properties.getProperty(RemoteTestNG.VERSION);
            adapter = ServiceLoaderHelper.getFirst(version == null ? null : new Version(version)).createSuiteDispatcherAdapter();
		}
		catch( Exception e)
		{
			throw new TestNGException( "Fail to initialize master mode", e);
		}
	}

	/**
	 * Dispatch test suites
	 * @return suites result
	 */
	public List<ISuite> dispatch(IConfiguration configuration,
	    List<XmlSuite> suites, String outputDir, List<ITestListener> testListeners){
		List<ISuite> result = Lists.newArrayList();
		try
		{
			//
			// Dispatch the suites/tests
			//

			for (XmlSuite suite : suites) {
				suite.setVerbose(m_verbose);
				SuiteRunner suiteRunner = new SuiteRunner(configuration, suite, outputDir);
				RemoteResultListener listener = new RemoteResultListener( suiteRunner);
				if (m_isStrategyTest) {
					for (XmlTest test : suite.getTests()) {
						XmlSuite tmpSuite = adapter.copy(suite, test);
						XmlTest tmpTest = adapter.copy(test, tmpSuite);
						m_masterAdapter.runSuitesRemotely(tmpSuite, listener);
					}
				}
				else
				{
					m_masterAdapter.runSuitesRemotely(suite, listener);
				}
				result.add(suiteRunner);
			}

			m_masterAdapter.awaitTermination(100_000);

			//
			// Run test listeners
			//
			for (ISuite suite : result) {
				for (ISuiteResult suiteResult : suite.getResults().values()) {
					Collection<ITestResult> allTests[] = new Collection[] {
							suiteResult.getTestContext().getPassedTests().getAllResults(),
							suiteResult.getTestContext().getFailedTests().getAllResults(),
							suiteResult.getTestContext().getSkippedTests().getAllResults(),
							suiteResult.getTestContext().getFailedButWithinSuccessPercentageTests().getAllResults(),
					};
					for (Collection<ITestResult> all : allTests) {
						for (ITestResult tr : all) {
							Invoker.runTestListeners(tr, testListeners);
						}
					}
				}
			}
		}
		catch( Exception ex)
		{
			//TODO add to logs
			ex.printStackTrace();
		}
		return result;
	}
}
