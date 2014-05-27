package de.osthus.ambeth.model;

import java.util.List;

public class DataChangeEventBO
{
	protected long id;

	protected byte version;

	protected long changeTime;

	protected List<DataChangeEntryBO> inserts;

	protected List<DataChangeEntryBO> updates;

	protected List<DataChangeEntryBO> deletes;

	protected DataChangeEventBO()
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

	public long getChangeTime()
	{
		return changeTime;
	}

	public void setChangeTime(long changeTime)
	{
		this.changeTime = changeTime;
	}

	public List<DataChangeEntryBO> getInserts()
	{
		return inserts;
	}

	public void setInserts(List<DataChangeEntryBO> inserts)
	{
		this.inserts = inserts;
	}

	public List<DataChangeEntryBO> getUpdates()
	{
		return updates;
	}

	public void setUpdates(List<DataChangeEntryBO> updates)
	{
		this.updates = updates;
	}

	public List<DataChangeEntryBO> getDeletes()
	{
		return deletes;
	}

	public void setDeletes(List<DataChangeEntryBO> deletes)
	{
		this.deletes = deletes;
	}
}
