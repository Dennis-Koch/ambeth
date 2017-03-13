package com.koch.ambeth.transfer;

public class MaterialSmallVO
{
	protected int id;

	protected short version;

	protected String buid;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setVersion(short version)
	{
		this.version = version;
	}

	public short getVersion()
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

	@Override
	public int hashCode()
	{
		return id ^ MaterialSmallVO.class.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MaterialSmallVO))
		{
			return false;
		}
		MaterialSmallVO other = (MaterialSmallVO) obj;
		return other.id == id && other.version == version;
	}
}
