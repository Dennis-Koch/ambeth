package com.koch.ambeth.query.backwards;

import com.koch.ambeth.model.AbstractEntity;

public class QueryEntity extends AbstractEntity
{
	protected String name;

	protected QueryEntity next;

	protected LinkTableEntity linkTableEntity;

	protected QueryEntity()
	{
		// Intended blank
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public QueryEntity getNext()
	{
		return next;
	}

	public void setNext(QueryEntity next)
	{
		this.next = next;
	}

	public LinkTableEntity getLinkTableEntity()
	{
		return linkTableEntity;
	}

	public void setLinkTableEntity(LinkTableEntity linkTableEntity)
	{
		this.linkTableEntity = linkTableEntity;
	}
}
