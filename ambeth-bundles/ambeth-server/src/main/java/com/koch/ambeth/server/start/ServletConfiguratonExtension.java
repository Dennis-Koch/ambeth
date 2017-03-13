package com.koch.ambeth.server.start;

import javax.servlet.ServletContext;

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.core.start.IAmbethConfiguration;
import com.koch.ambeth.core.start.IAmbethConfigurationExtension;
import com.koch.ambeth.server.webservice.HttpSessionModule;

public class ServletConfiguratonExtension implements IAmbethConfigurationExtension
{
	protected Ambeth ambethConfiguration;

	@Override
	public void setAmbethConfiguration(Ambeth ambethConfiguration)
	{
		this.ambethConfiguration = ambethConfiguration;
	}

	public IAmbethConfiguration withServletContext(ServletContext servletContext)
	{
		ambethConfiguration.registerBean(servletContext, ServletContext.class);
		ambethConfiguration.withProperty(CoreConfigurationConstants.ClasspathInfoClass, ServletClasspathInfo.class.getName());
		ambethConfiguration.withAmbethModules(HttpSessionModule.class);

		return ambethConfiguration;
	}
}
