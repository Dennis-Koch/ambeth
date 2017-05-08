package com.koch.ambeth.expr.bundle;

/*-
 * #%L
 * jambeth-information-bus-test
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
import org.junit.Test;

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.informationbus.InformationBus;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.util.IConversionHelper;

public class InformationBusTest {
	@Test
	public void testCreateBundle() throws IOException {
		IAmbethApplication ambethApplication = Ambeth.createBundle(InformationBus.class).start();
		Assert.assertNotNull(ambethApplication);
		try {
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			// Should be registered in root context
			Object service = serviceContext.getService(IConversionHelper.class, false);
			Assert.assertNotNull(service);

			// Should be registered in CacheModule (contained in the InformationBus bundle module)
			service = serviceContext.getService(ICacheHelper.class, false);
			Assert.assertNotNull(service);
		}
		finally {
			ambethApplication.close();
		}
	}
}
