package de.osthus.ambeth.model;

public class EntityType
{
	protected int id;

	protected byte version;

	protected Class<?> type;

	protected EntityType()
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

	public byte getVersion()
	{
		return version;
	}

	public void setVersion(byte version)
	{
		this.version = version;
	}

	public Class<?> getType()
	{
		return type;
	}

	public void setType(Class<?> type)
	{
		this.type = type;
	}
}
