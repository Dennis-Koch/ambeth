package de.osthus.ambeth.service;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;

@XmlType
public interface IPropertyCacheRetriever
{
	List<IObjRelationResult> getRelations(List<IObjRelation> objRelations);
}
