package de.osthus.ambeth.service;

import java.util.Collections;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.datachange.transfer.DataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.config.MergeServerConfigurationConstants;
import de.osthus.ambeth.merge.event.DataChangeOfSession;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.IDatabase;

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
