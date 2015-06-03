package de.osthus.ambeth.start;

import javax.servlet.ServletContext;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.config.CoreConfigurationConstants;

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
		return ambethConfiguration;
	}
}
