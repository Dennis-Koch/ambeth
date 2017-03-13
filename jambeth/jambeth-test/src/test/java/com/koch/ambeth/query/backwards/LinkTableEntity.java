package com.koch.ambeth.query.backwards;

import com.koch.ambeth.model.AbstractEntity;

public class LinkTableEntity extends AbstractEntity
{
	protected String name;

	protected JoinQueryEntity joinQueryEntity;

	protected LinkTableEntity()
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

	public JoinQueryEntity getJoinQueryEntity()
	{
		return joinQueryEntity;
	}

	public void setJoinQueryEntity(JoinQueryEntity joinQueryEntity)
	{
		this.joinQueryEntity = joinQueryEntity;
	}
}
