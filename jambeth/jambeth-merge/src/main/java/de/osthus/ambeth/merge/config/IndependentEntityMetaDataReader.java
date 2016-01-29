package de.osthus.ambeth.merge.config;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.IOrmConfigGroup;

public class IndependentEntityMetaDataReader extends AbstractEntityMetaDataReader implements IStartingBean
{
	@LogInstance
	private ILogger log;

	protected String xmlFileName = null;

	@Override
	public void afterStarted() throws Throwable
	{
		if (xmlFileName != null)
		{
			IOrmConfigGroup ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(xmlFileName);
			readConfig(ormConfigGroup);
		}
	}

	@Property(name = ServiceConfigurationConstants.mappingFile, mandatory = false)
	public void setFileName(String fileName)
	{
		if (xmlFileName != null)
		{
			throw new IllegalArgumentException("XmlDatabaseMapper already configured! Tried to set the config file '" + fileName
					+ "'. File name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = fileName;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Property(name = ServiceConfigurationConstants.mappingResource, mandatory = false)
	public void setResourceName(String xmlResourceName)
	{
		if (xmlFileName != null)
		{
			throw new IllegalArgumentException("EntityMetaDataReader already configured! Tried to set the config resource '" + xmlResourceName
					+ "'. Resource name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = xmlResourceName;
	}
}
