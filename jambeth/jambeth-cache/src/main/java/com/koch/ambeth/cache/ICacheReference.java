package com.koch.ambeth.cache;

public interface ICacheReference
{
	Class<?> getEntityType();

	Object getId();

	byte getIdIndex();
}
