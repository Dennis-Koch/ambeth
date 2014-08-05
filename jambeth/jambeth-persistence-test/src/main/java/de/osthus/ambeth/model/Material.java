package de.osthus.ambeth.model;

import java.util.Date;

import de.osthus.ambeth.annotation.EntityEqualsAspect;
import de.osthus.ambeth.annotation.XmlType;

@XmlType
@EntityEqualsAspect
public class Material
{
	protected int id;

	protected short version;

	protected String name;

	protected String buid;

	protected Unit unit;

	protected MaterialGroup materialGroup;

	protected Date createdOn;

	protected String createdBy;

	protected Date updatedOn;

	protected String updatedBy;

	protected Material()
	{
		// Intended blank
	}

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

	public Date getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(Date createdOn)
	{
		this.createdOn = createdOn;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	public Date getUpdatedOn()
	{
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn)
	{
		this.updatedOn = updatedOn;
	}

	public String getUpdatedBy()
	{
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy)
	{
		this.updatedBy = updatedBy;
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

	public void setUnit(Unit unit)
	{
		this.unit = unit;
	}

	public Unit getUnit()
	{
		return unit;
	}

	public void setMaterialGroup(MaterialGroup materialGroup)
	{
		this.materialGroup = materialGroup;
	}

	public MaterialGroup getMaterialGroup()
	{
		return materialGroup;
	}
}
