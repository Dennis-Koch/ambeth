package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.cache.model.IObjRelation;

public interface IPrimitiveRetriever
{
	Object[] getPrimitives(List<IObjRelation> objPropertyKeys);
}
