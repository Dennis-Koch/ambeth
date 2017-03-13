package com.koch.ambeth.transfer;

public class MaterialComplexVO
{
	protected int id;

	protected short version;

	protected String name;

	protected String buid;

	protected UnitVO unit;

	protected MaterialGroupVO materialGroup;

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

	public void setUnit(UnitVO unit)
	{
		this.unit = unit;
	}

	public UnitVO getUnit()
	{
		return unit;
	}

	public void setMaterialGroup(MaterialGroupVO materialGroup)
	{
		this.materialGroup = materialGroup;
	}

	public MaterialGroupVO getMaterialGroup()
	{
		return materialGroup;
	}

	@Override
	public int hashCode()
	{
		return id ^ MaterialComplexVO.class.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof MaterialComplexVO))
		{
			return false;
		}
		MaterialComplexVO other = (MaterialComplexVO) obj;
		return other.id == id && other.version == version;
	}
}
