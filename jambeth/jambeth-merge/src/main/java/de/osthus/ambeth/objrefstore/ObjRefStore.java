package de.osthus.ambeth.objrefstore;

import de.osthus.ambeth.merge.model.IObjRef;

public abstract class ObjRefStore implements IObjRef
{
	public static final int UNDEFINED_USAGE = -1;

	private ObjRefStore nextEntry;

	private int usageCount;

	public boolean isEqualTo(Class<?> entityType, byte idIndex, Object id)
	{
		return getId().equals(id) && getRealType().equals(entityType) && getIdNameIndex() == idIndex;
	}

	public ObjRefStore getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(ObjRefStore nextEntry)
	{
		this.nextEntry = nextEntry;
	}

	public void incUsageCount()
	{
		this.usageCount++;
	}

	public void decUsageCount()
	{
		this.usageCount--;
	}

	public void setUsageCount(int usageCount)
	{
		this.usageCount = usageCount;
	}

	public int getUsageCount()
	{
		return usageCount;
	}
}