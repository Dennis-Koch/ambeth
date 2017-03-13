package com.koch.ambeth.cache.service;

import java.util.List;

import com.koch.ambeth.service.cache.model.IObjRelation;

public interface IPrimitiveRetriever
{
	Object[] getPrimitives(List<IObjRelation> objPropertyKeys);
}
