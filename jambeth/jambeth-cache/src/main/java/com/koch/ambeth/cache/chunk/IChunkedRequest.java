package com.koch.ambeth.cache.chunk;

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IChunkedRequest
{
	IObjRelation getObjRelation();

	long getStartPosition();

	long getEndPosition();
}
