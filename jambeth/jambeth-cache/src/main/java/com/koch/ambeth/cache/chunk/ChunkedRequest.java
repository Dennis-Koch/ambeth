package com.koch.ambeth.cache.chunk;

import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public class ChunkedRequest implements IChunkedRequest
{
	protected IObjRelation objRelation;
	protected long startPosition;
	protected long endPosition;

	/**
	 * Needed for serialization
	 */
	public ChunkedRequest()
	{
		super();
	}

	public ChunkedRequest(IObjRelation objRelation, long startPosition, long endPosition)
	{
		super();
		this.objRelation = objRelation;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	@Override
	public IObjRelation getObjRelation()
	{
		return objRelation;
	}

	public void setObjRelation(IObjRelation objRelation)
	{
		this.objRelation = objRelation;
	}

	@Override
	public long getStartPosition()
	{
		return startPosition;
	}

	public void setStartPosition(long startPosition)
	{
		this.startPosition = startPosition;
	}

	@Override
	public long getEndPosition()
	{
		return endPosition;
	}

	public void setEndPosition(long endPosition)
	{
		this.endPosition = endPosition;
	}
}
