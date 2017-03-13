package com.koch.ambeth.merge;


public interface IEntityInstantiationExtension
{
	<T> Class<? extends T> getMappedEntityType(Class<T> type);
}
