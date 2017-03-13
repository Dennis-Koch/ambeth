package com.koch.ambeth.transfer;

public class MaterialVO
{
	protected int id;

	protected short version;

	protected String name;

	protected String buid;

	protected String unit;

	protected String materialGroup;

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

	public void setUnit(String unit)
	{
		this.unit = unit;
	}

	public String getUnit()
	{
		return unit;
	}

	public void setMaterialGroup(String materialGroup)
	{
		this.materialGroup = materialGroup;
	}

	public String getMaterialGroup()
	{
		return materialGroup;
	}

	@Override
	public int hashCode()
	{
		return id ^ MaterialVO.class.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MaterialVO))
		{
			return false;
		}
		MaterialVO other = (MaterialVO) obj;
		return other.id == id && other.version == version;
	}
}
