package com.koch.ambeth.query.backwards;

import com.koch.ambeth.model.AbstractEntity;

public class JoinQueryEntity extends AbstractEntity
{
	protected int value;

	protected QueryEntity queryEntity;

	protected JoinQueryEntity parent;

	protected LinkTableEntity linkTableEntity;

	protected JoinQueryEntity()
	{
		// Intended blank
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	public QueryEntity getQueryEntity()
	{
		return queryEntity;
	}

	public void setQueryEntity(QueryEntity queryEntity)
	{
		this.queryEntity = queryEntity;
	}

	public JoinQueryEntity getParent()
	{
		return parent;
	}

	public void setParent(JoinQueryEntity parent)
	{
		this.parent = parent;
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
