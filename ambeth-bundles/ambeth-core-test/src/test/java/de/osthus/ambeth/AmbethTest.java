package de.osthus.ambeth;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.bundle.Core;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.util.IConversionHelper;

public class AmbethTest
{
	// Basic create tests

	@Test
	public void testCreateDefault() throws IOException
	{
		IAmbethApplication ambethApplication = Ambeth.createDefault().start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			// Should be registered in root context
			Object service = serviceContext.getService(IConversionHelper.class, false);
			Assert.assertNotNull(service);

			// Should be registered in IoCModule
			service = serviceContext.getService("booleanArrayConverter", false);
			Assert.assertNotNull(service);
		}
		finally
		{
			ambethApplication.close();
		}
	}

	@Test
	public void testCreateBundle() throws IOException
	{
		IAmbethApplication ambethApplication = Ambeth.createBundle(Core.class).start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			// Should be registered in root context
			Object service = serviceContext.getService(IConversionHelper.class, false);
			Assert.assertNotNull(service);

			// Should be registered in IoCModule (contained in the Core bundle module)
			service = serviceContext.getService("booleanArrayConverter", false);
			Assert.assertNotNull(service);
		}
		finally
		{
			ambethApplication.close();
		}
	}

	@Test
	public void testCreateEmpty() throws IOException
	{
		IAmbethApplication ambethApplication = Ambeth.createEmpty().start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			// Should be registered in root context
			Object service = serviceContext.getService(IConversionHelper.class, false);
			Assert.assertNotNull(service);

			// Should be registered in IoCModule and should not be in this context
			service = serviceContext.getService("booleanArrayConverter", false);
			Assert.assertNull(service);
		}
		finally
		{
			ambethApplication.close();
		}
	}

	// Simple feature tests

	@Test
	public void testCreateWithProperty() throws IOException
	{
		String name = "testProp";
		String value = "test prop value";
		IAmbethApplication ambethApplication = Ambeth.createDefault().withProperty(name, value).start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();

			IProperties properties = serviceContext.getService(IProperties.class);
			Assert.assertNotNull(properties);
			Assert.assertEquals(value, properties.getString(name));
		}
		finally
		{
			ambethApplication.close();
		}
	}

	@Test
	public void testCreateWithAmbethModule() throws IOException
	{
		IAmbethApplication ambethApplication = Ambeth.createEmpty().withAmbethModules(IocModule.class).start();
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();

			// Should be registered in IoCModule
			Object service = serviceContext.getService("booleanArrayConverter", false);
			Assert.assertNotNull(service);
		}
		finally
		{
			ambethApplication.close();
		}
	}
}
