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

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ITable;

public class DirectedLink implements IDirectedLink
{
	@Property
	protected IDirectedLinkMetaData metaData;

	@Property
	protected ITable fromTable;

	@Property
	protected ITable toTable;

	@Property
	protected ILink link;

	@Property
	protected IDirectedLink reverse;

	@Override
	public IDirectedLinkMetaData getMetaData()
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
	public ILink getLink()
	{
		return link;
	}

	@Override
	public IDirectedLink getReverseLink()
	{
		return reverse;
	}

	@Override
	public ILinkCursor findLinked(Object fromId)
	{
		return link.findLinked(this, fromId);
	}

	@Override
	public ILinkCursor findLinkedTo(Object toId)
	{
		return link.findLinkedTo(getReverseLink(), toId);
	}

	@Override
	public ILinkCursor findAllLinked(List<?> fromIds)
	{
		return link.findAllLinked(this, fromIds);
	}

	@Override
	public void linkIds(Object fromId, List<?> toIds)
	{
		link.linkIds(this, fromId, toIds);
	}

	@Override
	public void updateLink(Object fromId, Object toId)
	{
		link.updateLink(this, fromId, toId);
	}

	@Override
	public void unlinkIds(Object fromId, List<?> toIds)
	{
		link.unlinkIds(this, fromId, toIds);
	}

	@Override
	public void unlinkAllIds(Object fromId)
	{
		link.unlinkAllIds(this, fromId);
	}

	@Override
	public String toString()
	{
		return "DirectedLink: " + getMetaData().getName();
	}
}
