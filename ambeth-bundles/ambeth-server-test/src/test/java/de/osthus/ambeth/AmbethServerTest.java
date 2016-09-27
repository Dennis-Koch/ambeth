package de.osthus.ambeth;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import de.osthus.ambeth.bundle.Core;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.start.ServletConfiguratonExtension;
import de.osthus.ambeth.testutil.BundleTestUtil;

public class AmbethServerTest
{
	// On the CI server the 'property.file' value is relative to the normal tests. The bundle tests have a different parent folder.
	@BeforeClass
	public static void beforeClass() throws IOException
	{
		BundleTestUtil.correctPropertyFilePath();
	}

	@Test
	public void testWithServletConfig() throws IOException
	{
		ServletContext mockedServletContext = Mockito.mock(ServletContext.class);

		IAmbethApplication ambethApplication = Ambeth.createBundle(Core.class) //
				.withExtension(ServletConfiguratonExtension.class).withServletContext(mockedServletContext) //
				.start();
		Assert.assertNotNull(ambethApplication);
		try
		{
			IServiceContext serviceContext = ambethApplication.getApplicationContext();
			Assert.assertNotNull(serviceContext);

			// Should be registered by the config extension
			ServletContext servletContext = serviceContext.getService(ServletContext.class, false);
			Assert.assertNotNull(servletContext);

			Mockito.verify(mockedServletContext, Mockito.times(2)).getResourcePaths("/WEB-INF/lib");
			Mockito.verify(mockedServletContext, Mockito.times(2)).getResourcePaths("/WEB-INF/classes");
		}
		finally
		{
			ambethApplication.close();
		}
	}
}
