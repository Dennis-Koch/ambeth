package com.koch.ambeth.expr.bundle;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.util.IConversionHelper;

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
