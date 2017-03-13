package com.koch.ambeth.util;

public class LongIdEntity
{
	protected Long id;

	protected short version;

	protected String updatedBy, createdBy;

	protected java.util.Date createdOn;

	protected java.sql.Date updatedOn;

	protected String name;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public short getVersion()
	{
		return version;
	}

	public void setVersion(short version)
	{
		this.version = version;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	public java.util.Date getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(java.util.Date createdOn)
	{
		this.createdOn = createdOn;
	}

	public String getUpdatedBy()
	{
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy)
	{
		this.updatedBy = updatedBy;
	}

	public java.sql.Date getUpdatedOn()
	{
		return updatedOn;
	}

	public void setUpdatedOn(java.sql.Date updatedOn)
	{
		this.updatedOn = updatedOn;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
