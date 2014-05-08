package de.osthus.ambeth.cache.model;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.merge.model.IObjRef;

@XmlType
public interface IObjRelationResult
{
	IObjRelation getReference();

	IObjRef[] getRelations();
}
