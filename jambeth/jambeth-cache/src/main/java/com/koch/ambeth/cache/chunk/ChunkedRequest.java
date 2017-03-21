package com.koch.ambeth.cache.chunk;

/*-
 * #%L
 * jambeth-cache
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
