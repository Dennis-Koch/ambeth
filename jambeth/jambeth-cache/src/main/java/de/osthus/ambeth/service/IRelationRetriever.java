package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;

public interface IRelationRetriever
{
	List<IObjRelationResult> getRelations(List<IObjRelation> objPropertyKeys);
}
