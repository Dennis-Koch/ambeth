package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.merge.model.IObjRef;

public interface ILoadContainerProvider
{

	void assignInstances(List<IObjRef> orisToLoad, List<ILoadContainer> targetEntities);

	void assignRelations(List<IObjRelation> orelsToLoad, List<IObjRelationResult> targetRelations);
}
