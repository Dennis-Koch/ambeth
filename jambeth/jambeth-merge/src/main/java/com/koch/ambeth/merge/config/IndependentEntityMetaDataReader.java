package com.koch.ambeth.merge.config;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.orm.IOrmConfigGroup;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

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
