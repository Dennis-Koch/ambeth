package de.osthus.ambeth.event;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.Lock;
import de.osthus.ambeth.util.LockState;
import de.osthus.ambeth.util.ReadWriteLock;

public class EventListenerRegistry implements IEventListenerExtendable, IEventBatcherExtendable, IEventTargetExtractorExtendable, IEventBatcher,
		IEventDispatcher, IEventListener, IEventQueue
{
	@LogInstance
	private ILogger log;

	protected final HashMap<Class<?>, List<IEventListenerMarker>> typeToListenersDict = new HashMap<Class<?>, List<IEventListenerMarker>>();

	protected final ClassExtendableContainer<IEventBatcher> typeToBatchersDict = new ClassExtendableContainer<IEventBatcher>("eventBatcher", "eventType");

	protected final ClassExtendableContainer<IEventTargetExtractor> typeToEventTargetExtractorsDict = new ClassExtendableContainer<IEventTargetExtractor>(
			"eventTargetExtractor", "eventType");

	protected final ArrayList<IEventListenerMarker> globalListenerList = new ArrayList<IEventListenerMarker>();

	protected final ThreadLocal<IList<IList<IQueuedEvent>>> eventQueueTL = new SensitiveThreadLocal<IList<IList<IQueuedEvent>>>();

	protected final IdentityLinkedSet<WaitForResumeItem> waitForResumeSet = new IdentityLinkedSet<WaitForResumeItem>();

	protected final LinkedHashSet<PausedEventTargetItem> pausedTargetsSet = new LinkedHashSet<PausedEventTargetItem>();

	protected final Lock readLock, writeLock;

	protected final Lock listenersReadLock, listenersWriteLock;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	public EventListenerRegistry()
	{
		ReadWriteLock rwLock = new ReadWriteLock();
		readLock = rwLock.getReadLock();
		writeLock = rwLock.getWriteLock();
		ReadWriteLock listenersRwLock = new ReadWriteLock();
		listenersReadLock = listenersRwLock.getReadLock();
		listenersWriteLock = listenersRwLock.getWriteLock();
	}

	@Override
	public void enableEventQueue()
	{
		IList<IList<IQueuedEvent>> eventQueueList = eventQueueTL.get();
		if (eventQueueList == null)
		{
			eventQueueList = new ArrayList<IList<IQueuedEvent>>();
			eventQueueTL.set(eventQueueList);
		}
		ArrayList<IQueuedEvent> eventQueue = new ArrayList<IQueuedEvent>();
		eventQueueList.add(eventQueue);
	}

	@Override
	public void flushEventQueue()
	{
		IList<IList<IQueuedEvent>> eventQueueList = eventQueueTL.get();
		if (eventQueueList == null)
		{
			return;
		}
		IList<IQueuedEvent> eventQueue = eventQueueList.remove(eventQueueList.size() - 1);
		if (eventQueueList.size() == 0)
		{
			eventQueueTL.remove();
			eventQueueList = null;
		}
		if (eventQueueList != null)
		{
			// Current flush is not the top-most flush. So we have to re-insert the events
			// One level out of our current level. We maintain the order at which the events have been queued
			IList<IQueuedEvent> outerEventQueue = eventQueueList.get(eventQueueList.size() - 1);
			for (int a = 0, size = eventQueue.size(); a < size; a++)
			{
				IQueuedEvent queuedEvent = eventQueue.get(a);
				outerEventQueue.add(queuedEvent);
			}
			return;
		}
		IList<IQueuedEvent> batchedEvents = batchEvents(eventQueue);
		for (int a = 0, size = batchedEvents.size(); a < size; a++)
		{
			IQueuedEvent batchedEvent = batchedEvents.get(a);
			handleEvent(batchedEvent.getEventObject(), batchedEvent.getDispatchTime(), batchedEvent.getSequenceNumber());
		}
	}

	@Override
	public IList<IQueuedEvent> batchEvents(List<IQueuedEvent> eventItems)
	{
		ArrayList<IQueuedEvent> outputEvents = new ArrayList<IQueuedEvent>();
		if (eventItems.size() == 0)
		{
			return outputEvents;
		}
		if (eventItems.size() == 1)
		{
			IQueuedEvent soleEvent = eventItems.get(0);
			outputEvents.add(soleEvent);
			return outputEvents;
		}
		ArrayList<IQueuedEvent> currentBatchableEvents = new ArrayList<IQueuedEvent>();

		IEventBatcher currentEventBatcher = null;
		for (int i = 0, size = eventItems.size(); i < size; i++)
		{
			IQueuedEvent queuedEvent = eventItems.get(i);
			Object eventObject = queuedEvent.getEventObject();
			IEventBatcher eventBatcher = typeToBatchersDict.getExtension(eventObject.getClass());
			if (eventBatcher != null)
			{
				if (currentEventBatcher != null && !eventBatcher.equals(currentEventBatcher))
				{
					IList<IQueuedEvent> batchedEvents = batchEventsIntern(currentBatchableEvents, currentEventBatcher);
					if (batchedEvents != null)
					{
						outputEvents.addAll(batchedEvents);
					}
					currentBatchableEvents.clear();
				}
				currentEventBatcher = eventBatcher;
				currentBatchableEvents.add(queuedEvent);
				continue;
			}
			IList<IQueuedEvent> batchedEvents = batchEventsIntern(currentBatchableEvents, currentEventBatcher);
			if (batchedEvents != null)
			{
				outputEvents.addAll(batchedEvents);
			}
			currentBatchableEvents.clear();
			currentEventBatcher = null;
			outputEvents.add(queuedEvent);
		}
		IList<IQueuedEvent> batchedEvents = batchEventsIntern(currentBatchableEvents, currentEventBatcher);
		if (batchedEvents != null)
		{
			outputEvents.addAll(batchedEvents);
		}
		return outputEvents;
	}

	protected IList<IQueuedEvent> batchEventsIntern(List<IQueuedEvent> currentBatchableEvents, IEventBatcher currentEventBatcher)
	{
		if (currentBatchableEvents.size() == 0)
		{
			return null;
		}
		if (currentBatchableEvents.size() == 1 || currentEventBatcher == null)
		{
			ArrayList<IQueuedEvent> batchedEvents = new ArrayList<IQueuedEvent>();
			for (int a = 0, size = currentBatchableEvents.size(); a < size; a++)
			{
				IQueuedEvent eventItem = currentBatchableEvents.get(a);
				batchedEvents.add(eventItem);
			}
			return batchedEvents;
		}
		else
		{
			return currentEventBatcher.batchEvents(currentBatchableEvents);
		}
	}

	@Override
	public void dispatchEvent(Object eventObject)
	{
		dispatchEvent(eventObject, System.currentTimeMillis(), -1);
	}

	@Override
	public void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		handleEvent(eventObject, dispatchTime, sequenceId);
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (eventObject == null)
		{
			return;
		}
		List<IList<IQueuedEvent>> tlEventQueueList = eventQueueTL.get();
		if (tlEventQueueList != null)
		{
			IList<IQueuedEvent> tlEventQueue = tlEventQueueList.get(tlEventQueueList.size() - 1);
			tlEventQueue.add(new QueuedEvent(eventObject, dispatchTime, sequenceId));
			return;
		}
		IdentityHashSet<IEventListenerMarker> interestedEventListeners;
		List<Object> pausedEventTargets;
		Lock listenersReadLock = this.listenersReadLock;
		listenersReadLock.lock();
		try
		{
			interestedEventListeners = new IdentityHashSet<IEventListenerMarker>();

			interestedEventListeners.addAll(globalListenerList); // Copy all global listeners as interested

			pausedEventTargets = evaluatePausedEventTargets();

			Class<?> currentType = eventObject.getClass();
			while (currentType != null)
			{
				evaluateType(currentType, interestedEventListeners);
				Class<?>[] interfaces = currentType.getInterfaces();
				for (Class<?> typeInterface : interfaces)
				{
					evaluateType(typeInterface, interestedEventListeners);
				}
				currentType = currentType.getSuperclass();
			}
		}
		finally
		{
			listenersReadLock.unlock();
		}
		for (IEventListenerMarker interestedEventListener : interestedEventListeners)
		{
			notifyEventListener(interestedEventListener, eventObject, null, pausedEventTargets, dispatchTime, sequenceId);
		}
	}

	protected IList<Object> evaluatePausedEventTargets()
	{
		LinkedHashSet<PausedEventTargetItem> pausedTargetsSet = this.pausedTargetsSet;
		ArrayList<Object> pausedEventTargets = new ArrayList<Object>(pausedTargetsSet.size());
		for (PausedEventTargetItem pauseETI : pausedTargetsSet)
		{
			pausedEventTargets.add(pauseETI.getEventTarget());
		}
		return pausedEventTargets;
	}

	protected void evaluateType(Class<?> type, Set<IEventListenerMarker> interestedEventListeners)
	{
		List<IEventListenerMarker> eventTypeListeners = typeToListenersDict.get(type);
		if (eventTypeListeners != null)
		{
			interestedEventListeners.addAll(eventTypeListeners);
		}
	}

	protected void evaluateTypeForEventTarget(Class<?> type, Set<IEventTargetEventListener> interestedEventListeners)
	{
		List<IEventListenerMarker> eventTypeListeners = typeToListenersDict.get(type);
		if (eventTypeListeners == null)
		{
			return;
		}
		for (int a = 0, size = eventTypeListeners.size(); a < size; a++)
		{
			IEventListenerMarker eventListener = eventTypeListeners.get(a);
			if (eventListener instanceof IEventTargetEventListener)
			{
				interestedEventListeners.add((IEventTargetEventListener) eventListener);
			}
		}
	}

	protected void notifyEventListener(IEventListenerMarker eventListener, Object eventObject, Object eventTarget, List<Object> pausedEventTargets,
			long dispatchTime, long sequenceId)
	{
		try
		{
			if (eventListener instanceof IEventTargetEventListener)
			{
				((IEventTargetEventListener) eventListener).handleEvent(eventObject, eventTarget, pausedEventTargets, dispatchTime, sequenceId);
			}
			else if (eventListener instanceof IEventListener)
			{
				((IEventListener) eventListener).handleEvent(eventObject, dispatchTime, sequenceId);
			}
		}
		catch (Exception e)
		{
			if (log.isErrorEnabled())
			{
				log.error(e);
			}
		}
	}

	@Override
	public void registerEventListener(IEventListenerMarker eventListener)
	{
		registerEventListener(eventListener, null);
	}

	@Override
	public void registerEventListener(IEventListenerMarker eventListener, Class<?> eventType)
	{
		if (eventListener == null)
		{
			throw new IllegalArgumentException("Argument must not be null: eventListener");
		}
		listenersWriteLock.lock();
		try
		{
			boolean success = true;
			try
			{
				if (eventType == null)
				{
					if (globalListenerList.contains(eventListener))
					{
						throw new IllegalArgumentException("Given eventListener already registered with this type");
					}
					globalListenerList.add(eventListener);
				}
				else
				{
					List<IEventListenerMarker> eventListeners = typeToListenersDict.get(eventType);
					if (eventListeners == null)
					{
						eventListeners = new ArrayList<IEventListenerMarker>();
						typeToListenersDict.put(eventType, eventListeners);
					}
					if (eventListeners.contains(eventListener))
					{
						throw new IllegalArgumentException("Given eventListener already registered with this type");
					}
					eventListeners.add(eventListener);
				}
				success = true;
			}
			finally
			{
				if (!success)
				{
					unregisterEventListenerForCleanup(eventListener);
				}
			}
		}
		finally
		{
			listenersWriteLock.unlock();
		}
	}

	@Override
	public void unregisterEventListener(IEventListenerMarker eventListener)
	{
		unregisterEventListener(eventListener, null);
	}

	@Override
	public void unregisterEventListener(IEventListenerMarker eventListener, Class<?> eventType)
	{
		if (eventListener == null)
		{
			throw new IllegalArgumentException("Argument must not be null: eventListener");
		}
		listenersWriteLock.lock();
		try
		{
			boolean success = false;
			try
			{
				if (eventType == null)
				{
					if (!globalListenerList.remove(eventListener))
					{
						throw new IllegalArgumentException("Given eventListener is not registered to this type");
					}
				}
				else
				{
					List<IEventListenerMarker> eventListeners = typeToListenersDict.get(eventType);
					if (eventListeners == null || !eventListeners.remove(eventListener))
					{
						throw new IllegalArgumentException("Given dataChangeListener is not registered to this type");
					}
					if (eventListeners != null && eventListeners.size() == 0)
					{
						typeToListenersDict.remove(eventType);
					}
				}
				success = true;
			}
			finally
			{
				if (!success)
				{
					unregisterEventListenerForCleanup(eventListener);
				}
			}
		}
		finally
		{
			listenersWriteLock.unlock();
		}
	}

	protected void unregisterEventListenerForCleanup(final IEventListenerMarker eventListener)
	{
		globalListenerList.remove(eventListener);
		for (Entry<Class<?>, List<IEventListenerMarker>> entry : typeToListenersDict)
		{
			List<IEventListenerMarker> eventListeners = entry.getValue();
			if (eventListeners != null)
			{
				eventListeners.remove(eventListener);
			}
		}
	}

	@Override
	public void registerEventBatcher(IEventBatcher eventBatcher, Class<?> eventType)
	{
		typeToBatchersDict.register(eventBatcher, eventType);
	}

	@Override
	public void unregisterEventBatcher(IEventBatcher eventBatcher, Class<?> eventType)
	{
		typeToBatchersDict.unregister(eventBatcher, eventType);
	}

	@Override
	public <T> T invokeWithoutLocks(IResultingBackgroundWorkerDelegate<T> runnable)
	{
		LockState lockState = writeLock.releaseAllLocks();
		try
		{
			return runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.reacquireLocks(lockState);
		}
	}

	@Override
	public void pause(Object eventTarget)
	{
		if (guiThreadHelper.isInGuiThread())
		{
			// nothing to do
			return;
		}
		IEventTargetExtractor eventTargetExtractor = typeToEventTargetExtractorsDict.getExtension(eventTarget.getClass());
		if (eventTargetExtractor != null)
		{
			eventTarget = eventTargetExtractor.extractEventTarget(eventTarget);
			if (eventTarget == null)
			{
				return;
			}
		}
		PausedEventTargetItem etiKey = new PausedEventTargetItem(eventTarget);
		Lock listenersWriteLock = this.listenersWriteLock;
		listenersWriteLock.lock();
		try
		{
			PausedEventTargetItem pauseETI = pausedTargetsSet.get(etiKey);
			if (pauseETI == null)
			{
				pauseETI = etiKey;
				pausedTargetsSet.add(pauseETI);
			}
			pauseETI.setPauseCount(pauseETI.getPauseCount() + 1);
		}
		finally
		{
			listenersWriteLock.unlock();
		}
	}

	@Override
	public void resume(Object eventTarget)
	{
		if (guiThreadHelper.isInGuiThread())
		{
			// nothing to do
			return;
		}
		IEventTargetExtractor eventTargetExtractor = typeToEventTargetExtractorsDict.getExtension(eventTarget.getClass());
		if (eventTargetExtractor != null)
		{
			eventTarget = eventTargetExtractor.extractEventTarget(eventTarget);
			if (eventTarget == null)
			{
				return;
			}
		}
		IdentityLinkedSet<WaitForResumeItem> freeLatchMap = null;
		try
		{
			PausedEventTargetItem etiKey = new PausedEventTargetItem(eventTarget);
			PausedEventTargetItem pauseETI;
			Lock listenersWriteLock = this.listenersWriteLock;
			listenersWriteLock.lock();
			try
			{
				LinkedHashSet<PausedEventTargetItem> pausedTargetsSet = this.pausedTargetsSet;
				pauseETI = pausedTargetsSet.get(etiKey);
				if (pauseETI == null)
				{
					throw new IllegalStateException("No pause() active for target " + eventTarget);
				}
				pauseETI.setPauseCount(pauseETI.getPauseCount() - 1);
				if (pauseETI.getPauseCount() > 0)
				{
					return;
				}
				pausedTargetsSet.remove(etiKey);
				if (waitForResumeSet.size() > 0)
				{
					IList<Object> remainingPausedEventTargets = evaluatePausedEventTargets();
					IdentityLinkedSet<Object> remainingPausedEventTargetsSet = IdentityLinkedSet.<Object> create(remainingPausedEventTargets.size());
					Iterator<WaitForResumeItem> iter = waitForResumeSet.iterator();
					while (iter.hasNext())
					{
						WaitForResumeItem pauseItem = iter.next();
						remainingPausedEventTargetsSet.addAll(remainingPausedEventTargets);
						remainingPausedEventTargetsSet.retainAll(pauseItem.pendingPauses);

						if (remainingPausedEventTargetsSet.size() == 0)
						{
							iter.remove();
							if (freeLatchMap == null)
							{
								freeLatchMap = new IdentityLinkedSet<WaitForResumeItem>();
							}
							freeLatchMap.add(pauseItem);
						}
						remainingPausedEventTargetsSet.clear();
					}
				}
			}
			finally
			{
				listenersWriteLock.unlock();
			}
		}
		finally
		{
			if (freeLatchMap != null)
			{
				for (WaitForResumeItem resumeItem : freeLatchMap)
				{
					resumeItem.getLatch().countDown();
				}
				for (WaitForResumeItem resumeItem : freeLatchMap)
				{
					try
					{
						resumeItem.getResultLatch().await();
					}
					catch (InterruptedException e)
					{
						throw RuntimeExceptionUtil.mask(e, "Fatal state occured. This may result in a global deadlock");
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void waitEventToResume(Object eventTargetToResume, long maxWaitTime, IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate,
			IBackgroundWorkerParamDelegate<Throwable> errorDelegate)
	{
		try
		{
			IdentityHashSet<Object> pendingSet = new IdentityHashSet<Object>();
			if (eventTargetToResume instanceof Collection)
			{
				pendingSet.addAll((Collection<Object>) eventTargetToResume);
			}
			else
			{
				pendingSet.add(eventTargetToResume);
			}
			WaitForResumeItem pauseItem = null;
			Lock listenersWriteLock = this.listenersWriteLock;
			listenersWriteLock.lock();
			try
			{
				IList<Object> remainingPausedEventTargets = evaluatePausedEventTargets();
				IdentityLinkedSet<Object> remainingPausedEventTargetsSet = new IdentityLinkedSet<Object>(remainingPausedEventTargets);
				remainingPausedEventTargetsSet.retainAll(pendingSet);

				if (remainingPausedEventTargetsSet.size() > 0)
				{
					// We should wait now but we have to check if we are in the UI thread, which must never wait
					if (guiThreadHelper.isInGuiThread())
					{
						// This is the trick: We "requeue" the current action in the UI pipeline to prohibit blocking
						// TODO: Currently not implemented because SyncContext or something similar does not exist yet
						// GuiThreadHelper.invokeInGuiLate(delegate()
						// {
						// waitEventToResume(eventTargetToResume, maxWaitTime, resumeDelegate, errorDelegate);
						// }, null);
						// return;
					}
					pauseItem = new WaitForResumeItem(pendingSet);
					waitForResumeSet.add(pauseItem);
				}
			}
			finally
			{
				listenersWriteLock.unlock();
			}
			if (pauseItem == null)
			{
				resumeDelegate.invoke(null);
				return;
			}
			CountDownLatch latch = pauseItem.getLatch();
			try
			{
				if (maxWaitTime < 0)
				{
					latch.await();
				}
				else if (maxWaitTime > 0)
				{
					latch.await(maxWaitTime, TimeUnit.MILLISECONDS);
				}
				else
				{
					throw new IllegalStateException("Thread should wait but does not want to");
				}
			}
			catch (InterruptedException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			resumeDelegate.invoke(pauseItem);
		}
		catch (Throwable e)
		{
			if (log.isErrorEnabled())
			{
				log.error(e);
			}
			if (errorDelegate != null)
			{
				try
				{
					errorDelegate.invoke(e);
				}
				catch (Throwable e1)
				{
					throw RuntimeExceptionUtil.mask(e1);
				}
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void registerEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Class<?> eventType)
	{
		typeToEventTargetExtractorsDict.register(eventTargetExtractor, eventType);
	}

	@Override
	public void unregisterEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Class<?> eventType)
	{
		typeToEventTargetExtractorsDict.unregister(eventTargetExtractor, eventType);
	}
}
