package com.koch.ambeth.datachange.model;

public class DirectDataChangeEntry implements IDataChangeEntry
{
	protected Object entry;

	public DirectDataChangeEntry(Object entry)
	{
		this.entry = entry;
	}

	public Object getEntry()
	{
		return entry;
	}

	@Override
	public Class<?> getEntityType()
	{
		return this.entry.getClass();
	}

	@Override
	public Object getId()
	{
		return null;
	}

	@Override
	public byte getIdNameIndex()
	{
		return -1;
	}

	@Override
	public Object getVersion()
	{
		return null;
	}

	@Override
	public String[] getTopics()
	{
		return null;
	}

	@Override
	public void setTopics(String[] topics)
	{
	}
}
