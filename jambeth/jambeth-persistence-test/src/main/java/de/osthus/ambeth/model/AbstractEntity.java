package de.osthus.ambeth.model;

public abstract class AbstractEntity extends AbstractBusinessObject
{
	public static final String Version = "Version";

	protected int id;

	protected short version;

	protected String updatedBy, createdBy;

	protected long updatedOn, createdOn;

	protected AbstractEntity()
	{
		// Intended blank
	}

	@Override
	public int getId()
	{
		return id;
	}

	public void setId(int id)
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
