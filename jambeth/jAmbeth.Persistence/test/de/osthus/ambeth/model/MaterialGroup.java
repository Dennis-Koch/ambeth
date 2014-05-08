package de.osthus.ambeth.model;

import de.osthus.ambeth.annotation.EntityEqualsAspect;
import de.osthus.ambeth.annotation.XmlType;

@XmlType
@EntityEqualsAspect
public class MaterialGroup
{
	protected String id;

	protected short version;

	protected String name;

	protected String buid;

	protected MaterialGroup()
	{
		// Intended blank
	}

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
