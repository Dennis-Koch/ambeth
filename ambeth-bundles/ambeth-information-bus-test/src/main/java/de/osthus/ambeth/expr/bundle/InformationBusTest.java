package de.osthus.ambeth.expr.bundle;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.bundle.InformationBus;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.util.ICacheHelper;
import de.osthus.ambeth.util.IConversionHelper;

public class InformationBusTest
{
	@Test
	public void testCreateBundle() throws IOException
	{
		IAmbethApplication ambethApplication = Ambeth.createBundle(InformationBus.class).start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			// Should be registered in root context
			Object service = serviceContext.getService(IConversionHelper.class, false);
			Assert.assertNotNull(service);

			// Should be registered in CacheModule (contained in the InformationBus bundle module)
			service = serviceContext.getService(ICacheHelper.class, false);
			Assert.assertNotNull(service);
		}
		finally
		{
			ambethApplication.close();
		}
	}
}
