package com.koch.ambeth.merge.config;

import com.koch.ambeth.merge.model.EntityMetaData;
import com.koch.ambeth.merge.orm.IEntityConfig;

public interface IEntityMetaDataReader
{
	void addMembers(EntityMetaData metaData, IEntityConfig entityConfig);
}