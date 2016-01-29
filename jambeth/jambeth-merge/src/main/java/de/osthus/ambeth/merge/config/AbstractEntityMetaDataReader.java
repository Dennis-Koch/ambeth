package de.osthus.ambeth.merge.config;

import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.orm.IEntityConfig;
import de.osthus.ambeth.orm.IOrmConfigGroup;
import de.osthus.ambeth.orm.IOrmConfigGroupProvider;

public abstract class AbstractEntityMetaDataReader implements IDisposableBean
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

	@Override
	public void destroy()
	{
		for (IEntityMetaData entityMetaData : managedEntityMetaData)
		{
			entityMetaDataExtendable.unregisterEntityMetaData(entityMetaData);
		}
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