package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.annotation.DataObjectAspect;

@DataObjectAspect
public abstract class TestEntityWithNonDefaultConstructor
{
	protected int id;

	protected short version;

	protected String name;

	protected TestEntityWithNonDefaultConstructor()
	{
		super();

		name = hashCode() + "";
	}

	protected TestEntityWithNonDefaultConstructor(String name)
	{
		super();

		this.name = name;
	}

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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
