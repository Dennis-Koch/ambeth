package de.osthus.ambeth.mapping;

public interface IDedicatedMapper
{
	void applySpecialMapping(Object businessObject, Object valueObject, CopyDirection direction);
}