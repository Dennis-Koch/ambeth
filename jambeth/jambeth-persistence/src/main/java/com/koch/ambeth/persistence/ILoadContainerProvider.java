package com.koch.ambeth.persistence;

import java.util.List;

import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface ILoadContainerProvider
{

	void assignInstances(List<IObjRef> orisToLoad, List<ILoadContainer> targetEntities);

	void assignRelations(List<IObjRelation> orelsToLoad, List<IObjRelationResult> targetRelations);
}
