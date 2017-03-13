package com.koch.ambeth.server.helloworld.vo;


public abstract class AbstractEntityVO
{
	protected long id;

	protected int version;

	protected String updatedBy, createdBy;

	protected long updatedOn, createdOn;

	public void setId(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public int getVersion()
	{
		return version;
	}

	public String getUpdatedBy()
	{
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy)
	{
		this.updatedBy = updatedBy;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	public long getUpdatedOn()
	{
		return updatedOn;
	}

	public void setUpdatedOn(long updatedOn)
	{
		this.updatedOn = updatedOn;
	}

	public long getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(long createdOn)
	{
		this.createdOn = createdOn;
	}

}
