package de.osthus.ambeth;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.converter.CharArrayConverter;

public class AmbethTest
{
	@Test
	public void testCreate() throws IOException
	{
		IAmbethApplication ambethApplication = Ambeth.create().start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			IConversionHelper conversionHelper = serviceContext.getService(IConversionHelper.class);
			Assert.assertNotNull(conversionHelper);
		}
		finally
		{
			ambethApplication.close();
		}
	}

	@Test
	public void testCreateWithProperty() throws IOException
	{
		String name = "testProp";
		String value = "test prop value";
		IAmbethApplication ambethApplication = Ambeth.create().withProperty(name, value).start();
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
		IAmbethApplication ambethApplication = Ambeth.create().withAmbethModules(IocModule.class).start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			CharArrayConverter charArrayConverter = serviceContext.getService("charArrayConverter", CharArrayConverter.class);
			Assert.assertNotNull(charArrayConverter);
		}
		finally
		{
			ambethApplication.close();
		}
	}
}
