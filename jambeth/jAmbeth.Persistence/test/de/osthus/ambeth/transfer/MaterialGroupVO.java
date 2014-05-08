package de.osthus.ambeth.transfer;

public class MaterialGroupVO
{
	protected String id;

	protected short version;

	protected String name;

	protected String buid;

	public void setVersion(short version)
	{
		this.version = version;
	}

	public short getVersion()
	{
		return version;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
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

	public void setBuid(String buid)
	{
		this.buid = buid;
	}

	public String getBuid()
	{
		return buid;
	}
}
