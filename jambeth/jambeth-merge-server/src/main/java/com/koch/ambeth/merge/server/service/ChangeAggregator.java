package com.koch.ambeth.merge.server.service;

import java.util.Collections;
import java.util.List;

import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.event.DataChangeOfSession;
import com.koch.ambeth.merge.server.config.MergeServerConfigurationConstants;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

public class ChangeAggregator implements IChangeAggregator
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Property(name = MergeServerConfigurationConstants.DeleteDataChangesByAlternateIds, defaultValue = "false")
	protected boolean deleteDataChangesByAlternateIds;

	protected IList<IDataChangeEntry> inserts;
	protected IList<IDataChangeEntry> updates;
	protected IList<IDataChangeEntry> deletes;

	@Override
	public void dataChangeInsert(IObjRef reference)
	{
		if (inserts == null)
		{
			inserts = new ArrayList<IDataChangeEntry>();
		}
		fillDataChange(inserts, reference);
	}

	@Override
	public void dataChangeUpdate(IObjRef reference)
	{
		if (updates == null)
		{
			updates = new ArrayList<IDataChangeEntry>();
		}
		fillDataChange(updates, reference);
	}

	@Override
	public void dataChangeDelete(IObjRef reference)
	{
		if (deletes == null)
		{
			deletes = new ArrayList<IDataChangeEntry>();
		}
		fillDataChange(deletes, reference);
	}

	@Override
	public void createDataChange()
	{
		List<IDataChangeEntry> inserts = this.inserts;
		List<IDataChangeEntry> updates = this.updates;
		List<IDataChangeEntry> deletes = this.deletes;
		if (eventDispatcher == null || (inserts == null && updates == null && deletes == null))
		{
			clear();
			return;
		}
		if (inserts == null)
		{
			inserts = Collections.emptyList();
		}
		else
		{
			inserts = new ArrayList<IDataChangeEntry>(inserts);
		}
		if (updates == null)
		{
			updates = Collections.emptyList();
		}
		else
		{
			updates = new ArrayList<IDataChangeEntry>(updates);
		}
		if (deletes == null)
		{
			deletes = Collections.emptyList();
		}
		else
		{
			deletes = new ArrayList<IDataChangeEntry>(deletes);
		}
		// Dispose all current lists in the aggregator
		clear();

		Long currentTime = database.getContextProvider().getCurrentTime();
		DataChangeEvent dataChange = new DataChangeEvent(inserts, updates, deletes, currentTime.longValue(), false);
		IDataChangeOfSession localDataChange = new DataChangeOfSession(database.getSessionId(), dataChange);
		eventDispatcher.dispatchEvent(localDataChange);
	}

	@Override
	public void clear()
	{
		inserts = null;
		updates = null;
		deletes = null;
	}

	protected void fillDataChange(List<IDataChangeEntry> dataChangeEntries, IObjRef ori)
	{
		if (ori.getIdNameIndex() != ObjRef.PRIMARY_KEY_INDEX && !deleteDataChangesByAlternateIds)
		{
			throw new RuntimeException("Implementation error: Only PK references are allowed in events");
		}
		DataChangeEntry dataChange = new DataChangeEntry(ori.getRealType(), ori.getIdNameIndex(), ori.getId(), ori.getVersion());
		dataChangeEntries.add(dataChange);
	}
}
