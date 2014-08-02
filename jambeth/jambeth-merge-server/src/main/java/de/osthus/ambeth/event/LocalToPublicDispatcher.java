package de.osthus.ambeth.event;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.event.LocalDataChangeEvent;

public class LocalToPublicDispatcher implements IEventListener
{
	protected final HashMap<Long, IList<IDataChange>> databaseToChangeDict = new HashMap<Long, IList<IDataChange>>();

	protected final Lock writeLock = new ReentrantLock();

	@Autowired
	protected IEventDispatcher publicEventDispatcher;

	@Override
	public void handleEvent(Object localEventObject, long dispatchTime, long sequenceId)
	{
		if (localEventObject instanceof DatabaseAcquireEvent)
		{
			DatabaseAcquireEvent localEvent = (DatabaseAcquireEvent) localEventObject;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				databaseToChangeDict.put(localEvent.sessionId, null);
			}
			finally
			{
				writeLock.unlock();
			}
		}
		else if (localEventObject instanceof DatabaseCommitEvent)
		{
			DatabaseCommitEvent localEvent = (DatabaseCommitEvent) localEventObject;
			IList<IDataChange> publicDataChanges;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				publicDataChanges = databaseToChangeDict.remove(localEvent.sessionId);
			}
			finally
			{
				writeLock.unlock();
			}
			if (publicDataChanges != null)
			{
				// TODO evtl. in Zukunft noch mit DataChangeEventBatcher die Anzahl reduzieren
				IEventDispatcher publicEventDispatcher = this.publicEventDispatcher;
				for (int i = 0; i < publicDataChanges.size(); i++)
				{
					IDataChange publicDataChange = publicDataChanges.get(i);
					publicEventDispatcher.dispatchEvent(publicDataChange, dispatchTime, sequenceId);
				}
			}
		}
		else if (localEventObject instanceof DatabaseFailEvent)
		{
			DatabaseFailEvent localEvent = (DatabaseFailEvent) localEventObject;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				databaseToChangeDict.remove(localEvent.sessionId);
			}
			finally
			{
				writeLock.unlock();
			}
		}
		else if (localEventObject instanceof LocalDataChangeEvent)
		{
			LocalDataChangeEvent localEvent = (LocalDataChangeEvent) localEventObject;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				IList<IDataChange> dataChanges = databaseToChangeDict.get(localEvent.getSessionID());
				if (dataChanges == null)
				{
					dataChanges = new ArrayList<IDataChange>();
					databaseToChangeDict.put(localEvent.getSessionID(), dataChanges);
				}
				dataChanges.add(localEvent.getDataChange());
			}
			finally
			{
				writeLock.unlock();
			}
		}
	}
}
