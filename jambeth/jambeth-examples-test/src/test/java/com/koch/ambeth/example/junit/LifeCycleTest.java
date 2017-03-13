package com.koch.ambeth.example.junit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.koch.ambeth.example.junit.LifeCycleTest.LifeCycleTestFrameworkModule;
import com.koch.ambeth.example.junit.LifeCycleTest.LifeCycleTestModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestModule;

@TestFrameworkModule(LifeCycleTestFrameworkModule.class)
@TestModule(LifeCycleTestModule.class)
public class LifeCycleTest extends AbstractIocTest
{
	private static final ILogger LOG = LoggerFactory.getLogger(LifeCycleTest.class);

	public static class LifeCycleTestModule implements IInitializingModule
	{
		@LogInstance
		private ILogger log;

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			log.debug("LifeCycleTestModule.afterPropertiesSet()");
		}
	}

	public static class LifeCycleTestFrameworkModule implements IInitializingModule
	{
		@LogInstance
		private ILogger log;

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			log.debug("LifeCycleTestFrameworkModule.afterPropertiesSet()");
		}

	}

	static
	{
		LOG.debug("LifeCycleTest.static");
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		LOG.debug("LifeCycleTest.setUpBeforeClass()");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		LOG.debug("LifeCycleTest.tearDownAfterClass()");
	}

	public LifeCycleTest()
	{
		LOG.debug("LifeCycleTest.LifeCycleTest()");
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		LOG.debug("LifeCycleTest.afterPropertiesSet()");
	}

	@Before
	public void setUp() throws Exception
	{
		LOG.debug("LifeCycleTest.setUp()");
	}

	@After
	public void tearDown() throws Exception
	{
		LOG.debug("LifeCycleTest.tearDown()");
	}

	@Test
	public void test1()
	{
		LOG.debug("LifeCycleTest.test1()");
	}

	@Test
	public void test2()
	{
		LOG.debug("LifeCycleTest.test2()");
	}
}
