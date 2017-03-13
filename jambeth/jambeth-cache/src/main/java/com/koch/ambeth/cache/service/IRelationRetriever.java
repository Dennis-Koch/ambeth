package com.koch.ambeth.cache.service;

import java.util.List;

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;

public interface IRelationRetriever
{
	List<IObjRelationResult> getRelations(List<IObjRelation> objPropertyKeys);
}
