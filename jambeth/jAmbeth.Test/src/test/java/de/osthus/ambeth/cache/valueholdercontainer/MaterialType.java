package de.osthus.ambeth.cache.valueholdercontainer;

import de.osthus.ambeth.annotation.EntityEqualsAspect;
import de.osthus.ambeth.annotation.FireTargetOnPropertyChange;
import de.osthus.ambeth.annotation.FireThisOnPropertyChange;

@EntityEqualsAspect
public class MaterialType
{
	private int id;

	private int version;

	private String name;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	@FireTargetOnPropertyChange("Temp2")
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@FireThisOnPropertyChange("Name")
	public String getTemp1()
	{
		return getName() + "$Temp1";
	}

	public String getTemp2()
	{
		return getName() + "$Temp2";
	}
}
