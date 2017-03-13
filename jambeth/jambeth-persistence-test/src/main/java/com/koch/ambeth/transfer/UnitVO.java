package com.koch.ambeth.transfer;

public class UnitVO
{
	protected Object id;

	protected String name;

	protected long version;

	protected String buid;

	public void setId(Object id)
	{
		this.id = id;
	}

	public Object getId()
	{
		return id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setVersion(long version)
	{
		this.version = version;
	}

	public long getVersion()
	{
		return version;
	}

	public void setBuid(String buid)
	{
		this.buid = buid;
	}

	public String getBuid()
	{
		return buid;
	}
}
