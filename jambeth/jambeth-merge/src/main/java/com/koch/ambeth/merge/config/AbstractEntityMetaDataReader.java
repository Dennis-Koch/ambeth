package com.koch.ambeth.merge.config;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityMetaDataExtendable;
import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.merge.orm.IEntityConfig;
import com.koch.ambeth.merge.orm.IOrmConfigGroup;
import com.koch.ambeth.merge.orm.IOrmConfigGroupExtendable;
import com.koch.ambeth.merge.orm.IOrmConfigGroupProvider;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;

import java.util.List;

public abstract class AbstractEntityMetaDataReader implements IDisposableBean {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEntityMetaDataExtendable entityMetaDataExtendable;

	@Autowired
	protected IEntityMetaDataReader entityMetaDataReader;

	@Autowired
	protected IOrmConfigGroupProvider ormConfigGroupProvider;

	@Autowired(optional = true)
	protected IOrmConfigGroupExtendable ormConfigGroupExtendable;

	protected final LinkedHashSet<IEntityMetaData> managedEntityMetaData = new LinkedHashSet<>();

	protected final List<IOrmConfigGroup> ormConfigGroups = new ArrayList<>();

	@Override
	public void destroy() {
		for (var ormConfigGroup : ormConfigGroups) {
			ormConfigGroupExtendable.unregisterOrmConfigGroup(ormConfigGroup);
		}
		for (var entityMetaData : managedEntityMetaData) {
			entityMetaDataExtendable.unregisterEntityMetaData(entityMetaData);
		}
	}

	protected void readConfig(IOrmConfigGroup ormConfigGroup) {
		var entities = new LinkedHashSet<IEntityConfig>();
		entities.addAll(ormConfigGroup.getLocalEntityConfigs());
		entities.addAll(ormConfigGroup.getExternalEntityConfigs());

		for (var entityConfig : entities) {
			var entityType = entityConfig.getEntityType();
			if (entityMetaDataProvider.getMetaData(entityType, true) != null) {
				continue;
			}
			var realType = entityConfig.getRealType();

			var metaData = new EntityMetaData();
			metaData.setEntityType(entityType);
			metaData.setRealType(realType);
			metaData.setLocalEntity(entityConfig.isLocal());

			entityMetaDataReader.addMembers(metaData, entityConfig);

			managedEntityMetaData.add(metaData);
			synchronized (entityMetaDataExtendable) {
				entityMetaDataExtendable.registerEntityMetaData(metaData);
			}
		}
		if (ormConfigGroupExtendable != null) {
			ormConfigGroupExtendable.registerOrmConfigGroup(ormConfigGroup);
			ormConfigGroups.add(ormConfigGroup);
		}
	}
}
