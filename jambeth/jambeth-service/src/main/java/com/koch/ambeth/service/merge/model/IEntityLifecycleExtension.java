package com.koch.ambeth.service.merge.model;

public interface IEntityLifecycleExtension
{
	void postCreate(IEntityMetaData metaData, Object newEntity);

	void postLoad(IEntityMetaData metaData, Object entity);

	void prePersist(IEntityMetaData metaData, Object entity);
}
