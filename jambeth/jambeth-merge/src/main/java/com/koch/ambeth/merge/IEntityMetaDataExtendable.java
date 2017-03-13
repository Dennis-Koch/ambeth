package com.koch.ambeth.merge;

import com.koch.ambeth.service.merge.model.IEntityMetaData;

public interface IEntityMetaDataExtendable
{
	void registerEntityMetaData(IEntityMetaData entityMetaData);

	void unregisterEntityMetaData(IEntityMetaData entityMetaData);

	void registerEntityMetaData(IEntityMetaData entityMetaData, Class<?> entityType);

	void unregisterEntityMetaData(IEntityMetaData entityMetaData, Class<?> entityType);

}
