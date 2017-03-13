package com.koch.ambeth.service.cache.model;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IObjRelationResult
{
	IObjRelation getReference();

	IObjRef[] getRelations();
}
