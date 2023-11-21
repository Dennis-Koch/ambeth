package com.koch.ambeth.server;

/*-
 * #%L
 * jambeth-server-test
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

import jakarta.servlet.ServletContext;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.bundle.Core;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.server.start.ServletConfiguratonExtension;
import com.koch.ambeth.testutil.BundleTestUtil;

public class AmbethServerTest {
	// On the CI server the 'property.file' value is relative to the normal tests. The bundle tests
	// have a different parent folder.
	@BeforeClass
	public static void beforeClass() throws IOException {
		BundleTestUtil.correctPropertyFilePath();
	}

	@Test
	public void testWithServletConfig() throws IOException {
		ServletContext mockedServletContext = Mockito.mock(ServletContext.class);

		IAmbethApplication ambethApplication = Ambeth.createBundle(Core.class) //
				.withExtension(ServletConfiguratonExtension.class).withServletContext(mockedServletContext) //
				.start();
		Assert.assertNotNull(ambethApplication);
		try {
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			// Should be registered by the config extension
			ServletContext servletContext = serviceContext.getService(ServletContext.class, false);
			Assert.assertNotNull(servletContext);

			Mockito.verify(mockedServletContext, Mockito.times(2)).getResourcePaths("/WEB-INF/lib");
			Mockito.verify(mockedServletContext, Mockito.times(2)).getResourcePaths("/WEB-INF/classes");
		}
		finally {
			ambethApplication.close();
		}
	}
}
