package com.koch.ambeth.query.squery.model;

import com.koch.ambeth.util.annotation.EntityEqualsAspect;

@EntityEqualsAspect
public abstract class BaseEntity
{
	protected Integer id;
	protected Integer version;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Integer getVersion()
	{
		return version;
	}

	public void setVersion(Integer version)
	{
		this.version = version;
	}
}
