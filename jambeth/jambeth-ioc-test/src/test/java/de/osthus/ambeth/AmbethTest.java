package de.osthus.ambeth;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.IAmbethApplication;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.util.IConversionHelper;

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
}
