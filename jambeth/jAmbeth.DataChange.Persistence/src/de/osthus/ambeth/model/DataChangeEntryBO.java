package de.osthus.ambeth.model;

public class DataChangeEntryBO
{
	protected long id;

	protected byte version;

	protected EntityType entityType;

	protected byte idIndex;

	protected String objectId;

	protected String objectVersion;

	protected DataChangeEntryBO()
	{
		// Intended blank
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
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

	public EntityType getEntityType()
	{
		return entityType;
	}

	public void setEntityType(EntityType entityType)
	{
		this.entityType = entityType;
	}

	public byte getIdIndex()
	{
		return idIndex;
	}

	public void setIdIndex(byte idIndex)
	{
		this.idIndex = idIndex;
	}

	public String getObjectId()
	{
		return objectId;
	}

	public void setObjectId(String objectId)
	{
		this.objectId = objectId;
	}

	public String getObjectVersion()
	{
		return objectVersion;
	}

	public void setObjectVersion(String objectVersion)
	{
		this.objectVersion = objectVersion;
	}
}
