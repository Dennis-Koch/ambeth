package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.merge.model.IObjRef;

@XmlType
public interface ICacheRetriever
{
	List<ILoadContainer> getEntities(List<IObjRef> orisToLoad);

	List<IObjRelationResult> getRelations(List<IObjRelation> objRelations);
}
