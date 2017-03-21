package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.util.IAlreadyLinkedCache;
import com.koch.ambeth.util.ParamChecker;

public class Link implements ILink, IInitializingBean
{
	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Property
	protected ILinkMetaData metaData;

	@Property
	protected ITable fromTable;

	@Property(mandatory = false)
	protected ITable toTable;

	@Property
	protected IDirectedLink directedLink;

	@Property
	protected IDirectedLink reverseDirectedLink;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertTrue(toTable != null || directedLink.getMetaData().getToMember() != null, "toTable or toMember");
	}

	@Override
	public ILinkMetaData getMetaData()
	{
		return metaData;
	}

	@Override
	public ITable getFromTable()
	{
		return fromTable;
	}

	@Override
	public ITable getToTable()
	{
		return toTable;
	}

	@Override
	public IDirectedLink getDirectedLink()
	{
		return directedLink;
	}

	@Override
	public IDirectedLink getReverseDirectedLink()
	{
		return reverseDirectedLink;
	}

	@Override
	public ILinkCursor findAllLinked(IDirectedLink fromLink, List<?> fromIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ILinkCursor findAllLinkedTo(IDirectedLink fromLink, List<?> toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ILinkCursor findLinked(IDirectedLink fromLink, Object fromId)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ILinkCursor findLinkedTo(IDirectedLink fromLink, Object toId)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void linkIds(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void updateLink(IDirectedLink fromLink, Object fromId, Object toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void unlinkIds(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void unlinkAllIds(IDirectedLink fromLink, Object fromId)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void unlinkAllIds()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected boolean addLinkModToCache(IDirectedLink fromLink, Object fromId, Object toId)
	{
		Object leftId, rightId;
		if (getDirectedLink() == fromLink)
		{
			leftId = fromId;
			rightId = toId;
		}
		else if (getReverseDirectedLink() == fromLink)
		{
			leftId = toId;
			rightId = fromId;
		}
		else
		{
			throw new IllegalArgumentException("Invalid link " + fromLink);
		}
		return alreadyLinkedCache.put(this, leftId, rightId);
	}

	@Override
	public String toString()
	{
		return "Link: " + getMetaData().getName();
	}

	@Override
	public void startBatch()
	{
		// Intended blank
	}

	@Override
	public int[] finishBatch()
	{
		return new int[0];
	}

	@Override
	public void clearBatch()
	{
		// Intended blank
	}
}
