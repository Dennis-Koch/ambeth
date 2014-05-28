package de.osthus.ambeth.merge.config;

import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.orm.EntityConfig;

public interface IEntityMetaDataReader
{
	void addMembers(EntityMetaData metaData, EntityConfig entityConfig);
}