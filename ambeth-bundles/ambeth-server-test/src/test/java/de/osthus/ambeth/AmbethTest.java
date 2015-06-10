package de.osthus.ambeth;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import de.osthus.ambeth.bundle.Core;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.start.ServletConfiguratonExtension;

public class AmbethTest
{
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

			Mockito.verify(mockedServletContext).getResourcePaths("/WEB-INF/lib");
			Mockito.verify(mockedServletContext).getResourcePaths("/WEB-INF/classes");
		}
		finally
		{
			ambethApplication.close();
		}
	}
}
