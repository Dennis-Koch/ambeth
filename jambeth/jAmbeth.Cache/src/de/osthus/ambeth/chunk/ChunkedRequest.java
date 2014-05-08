package de.osthus.ambeth.chunk;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.cache.model.IObjRelation;

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
