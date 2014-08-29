package de.osthus.ambeth.event;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.ioc.annotation.Autowired;

public class LocalToPublicDispatcher implements IEventListener
{
	@Autowired
	protected IEventDispatcher publicEventDispatcher;

	protected final HashMap<Long, ArrayList<IDataChange>> databaseToChangeDict = new HashMap<Long, ArrayList<IDataChange>>();

	protected final Lock writeLock = new ReentrantLock();

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
				IEventDispatcher publicEventDispatcher = this.publicEventDispatcher;
				if (publicDataChanges.size() > 1)
				{
					publicEventDispatcher.enableEventQueue();
				}
				for (int i = 0; i < publicDataChanges.size(); i++)
				{
					IDataChange publicDataChange = publicDataChanges.get(i);
					publicEventDispatcher.dispatchEvent(publicDataChange, dispatchTime, sequenceId);
				}
				if (publicDataChanges.size() > 1)
				{
					publicEventDispatcher.flushEventQueue();
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
		else if (localEventObject instanceof IDataChangeOfSession)
		{
			IDataChangeOfSession localEvent = (IDataChangeOfSession) localEventObject;
			Lock writeLock = this.writeLock;
			writeLock.lock();
			try
			{
				ArrayList<IDataChange> dataChanges = databaseToChangeDict.get(localEvent.getSessionId());
				if (dataChanges == null)
				{
					dataChanges = new ArrayList<IDataChange>();
					databaseToChangeDict.put(localEvent.getSessionId(), dataChanges);
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
