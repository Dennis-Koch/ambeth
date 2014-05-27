package de.osthus.ambeth.datachange.store;

import de.osthus.ambeth.merge.transfer.ObjRef;

public class ObjRefStore extends ObjRef
{
	public static final int UNDEFINED_USAGE = -1;

	protected int usageCount;

	public ObjRefStore()
	{
		super();
	}

	public ObjRefStore(Class<?> realType, byte idNameIndex, Object id, Object version)
	{
		super(realType, idNameIndex, id, version);
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