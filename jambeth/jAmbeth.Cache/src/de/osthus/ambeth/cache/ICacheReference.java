package de.osthus.ambeth.cache;

public interface ICacheReference
{
	Class<?> getEntityType();

	Object getId();

	byte getIdIndex();
}
