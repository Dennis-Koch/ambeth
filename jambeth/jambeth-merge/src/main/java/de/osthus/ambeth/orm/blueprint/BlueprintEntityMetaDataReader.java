package de.osthus.ambeth.orm.blueprint;

import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.orm.IEntityConfig;
import de.osthus.ambeth.orm.IOrmConfigGroup;
import de.osthus.ambeth.orm.IOrmConfigGroupProvider;

public class BlueprintEntityMetaDataReader implements IStartingBean, IDisposableBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEntityMetaDataExtendable entityMetaDataExtendable;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IEntityMetaDataReader entityMetaDataReader;

	@Autowired
	protected IOrmConfigGroupProvider ormConfigGroupProvider;

	protected final LinkedHashSet<IEntityMetaData> managedEntityMetaData = new LinkedHashSet<IEntityMetaData>();

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

	@Override
	public void destroy()
	{
		for (IEntityMetaData entityMetaData : managedEntityMetaData)
		{
			entityMetaDataExtendable.unregisterEntityMetaData(entityMetaData);
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

	protected void readConfig(IOrmConfigGroup ormConfigGroup)
	{
		LinkedHashSet<IEntityConfig> entities = new LinkedHashSet<IEntityConfig>();
		entities.addAll(ormConfigGroup.getLocalEntityConfigs());
		entities.addAll(ormConfigGroup.getExternalEntityConfigs());

		for (IEntityConfig entityConfig : entities)
		{
			Class<?> entityType = entityConfig.getEntityType();
			if (entityMetaDataProvider.getMetaData(entityType, true) != null)
			{
				continue;
			}
			Class<?> realType = entityConfig.getRealType();

			EntityMetaData metaData = new EntityMetaData();
			metaData.setEntityType(entityType);
			metaData.setRealType(realType);
			metaData.setLocalEntity(entityConfig.isLocal());

			entityMetaDataReader.addMembers(metaData, entityConfig);

			managedEntityMetaData.add(metaData);
			synchronized (entityMetaDataExtendable)
			{
				entityMetaDataExtendable.registerEntityMetaData(metaData);
			}
		}
	}
}
