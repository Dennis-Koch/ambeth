package com.koch.ambeth;

/*-
 * #%L
 * jambeth-core-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.bundle.Core;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.testutil.BundleTestUtil;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.config.IProperties;

public class AmbethTest
{
	// On the CI server the 'property.file' value is relative to the normal tests. The bundle tests have a different parent folder.
	@BeforeClass
	public static void beforeClass() throws IOException
	{
		BundleTestUtil.correctPropertyFilePath();
	}

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
