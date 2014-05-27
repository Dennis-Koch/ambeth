package de.osthus.ambeth.chunk;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.cache.model.IObjRelation;

@XmlType
public interface IChunkedRequest
{
	IObjRelation getObjRelation();

	long getStartPosition();

	long getEndPosition();
}
