package de.osthus.ambeth.orm20.independent;

public class EntityB
{
	protected int idB;

	protected short versionB;

	protected String updatedByB, createdByB;

	protected long updatedOnB, createdOnB;

	public int getIdB()
	{
		return idB;
	}

	public void setIdB(int idB)
	{
		this.idB = idB;
	}

	public short getVersionB()
	{
		return versionB;
	}

	public void setVersionB(short versionB)
	{
		this.versionB = versionB;
	}

	public String getUpdatedByB()
	{
		return updatedByB;
	}

	public void setUpdatedByB(String updatedByB)
	{
		this.updatedByB = updatedByB;
	}

	public String getCreatedByB()
	{
		return createdByB;
	}

	public void setCreatedByB(String createdByB)
	{
		this.createdByB = createdByB;
	}

	public long getUpdatedOnB()
	{
		return updatedOnB;
	}

	public void setUpdatedOnB(long updatedOnB)
	{
		this.updatedOnB = updatedOnB;
	}

	public long getCreatedOnB()
	{
		return createdOnB;
	}

	public void setCreatedOnB(long createdOnB)
	{
		this.createdOnB = createdOnB;
	}
}
