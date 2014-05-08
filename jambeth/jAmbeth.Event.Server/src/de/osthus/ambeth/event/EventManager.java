package de.osthus.ambeth.event;

import java.util.Collections;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IListElem;
import de.osthus.ambeth.collections.InterfaceFastList;
import de.osthus.ambeth.event.model.IEventItem;
import de.osthus.ambeth.event.store.IEventStoreHandler;
import de.osthus.ambeth.event.store.IEventStoreHandlerExtendable;
import de.osthus.ambeth.event.store.IReplacedEvent;
import de.osthus.ambeth.event.transfer.EventItem;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.util.ParamChecker;

public class EventManager implements IEventProvider, IEventStore, IEventListener, IEventStoreHandlerExtendable, IInitializingBean
{
	protected final InterfaceFastList<IQueuedEvent> eventQueue = new InterfaceFastList<IQueuedEvent>();

	protected volatile long eventSequence;

	protected long lastCleanup;

	protected long maxEventHistoryTime;
	protected long maxResponseDelay;
	protected long minCleanupDelay;

	protected final ClassExtendableContainer<IEventStoreHandler> eventStoreHandlers = new ClassExtendableContainer<IEventStoreHandler>("eventStoreHandler",
			"eventType");

	protected IEventBatcher eventBatcher;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(eventBatcher, "EventBatcher");
	}

	public void setEventBatcher(IEventBatcher eventBatcher)
	{
		this.eventBatcher = eventBatcher;
	}

	public long getMaxEventHistoryTime()
	{
		return maxEventHistoryTime;
	}

	public void setMaxEventHistoryTime(long maxEventHistoryTime)
	{
		this.maxEventHistoryTime = maxEventHistoryTime;
	}

	public long getMaxResponseDelay()
	{
		return maxResponseDelay;
	}

	public void setMaxResponseDelay(long maxResponseDelay)
	{
		this.maxResponseDelay = maxResponseDelay;
	}

	public long getMinCleanupDelay()
	{
		return minCleanupDelay;
	}

	public void setMinCleanupDelay(long minCleanupDelay)
	{
		this.minCleanupDelay = minCleanupDelay;
	}

	public EventManager()
	{
		maxEventHistoryTime = 10L * 60L * 1000L;// Minutes(10);
		maxResponseDelay = 60L * 1000L; // Seconds(60);
		minCleanupDelay = 1L * 1000L; // Seconds(1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		eventObject = preSaveToStore(eventObject);
		synchronized (eventQueue)
		{
			checkEventHistoryForCleanupIntern();
			IListElem<IQueuedEvent> queuedEventLE = null;
			long sequenceNumber = ++eventSequence;
			if (eventObject instanceof IListElem)
			{
				Object listElemTarget = ((IListElem<?>) eventObject).getElemValue();
				if (listElemTarget instanceof IQueuedEvent)
				{
					queuedEventLE = (IListElem<IQueuedEvent>) listElemTarget;
					IQueuedEvent queuedEvent = ((IQueuedEvent) listElemTarget);
					queuedEvent.setDispatchTime(dispatchTime);
					queuedEvent.setSequenceNumber(sequenceNumber);
				}
			}
			if (queuedEventLE == null)
			{
				queuedEventLE = new QueuedEvent(eventObject, dispatchTime, sequenceNumber);
			}
			eventQueue.pushLast(queuedEventLE);
			eventQueue.notifyAll();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addEvents(List<Object> eventObjects)
	{
		for (int i = eventObjects.size(); i-- > 0;)
		{
			Object eventObject = eventObjects.get(i);
			eventObject = preSaveToStore(eventObject);
			eventObjects.set(i, eventObject);
		}
		synchronized (eventQueue)
		{
			checkEventHistoryForCleanupIntern();
			long dispatchTime = System.currentTimeMillis();
			for (int i = 0, size = eventObjects.size(); i < size; i++)
			{
				Object eventObject = eventObjects.get(i);
				IListElem<IQueuedEvent> queuedEventLE = null;
				long sequenceNumber = ++eventSequence;
				if (eventObject instanceof IListElem)
				{
					Object listElemTarget = ((IListElem<?>) eventObject).getElemValue();
					if (listElemTarget instanceof IQueuedEvent)
					{
						queuedEventLE = (IListElem<IQueuedEvent>) listElemTarget;
						IQueuedEvent queuedEvent = ((IQueuedEvent) listElemTarget);
						queuedEvent.setDispatchTime(dispatchTime);
						queuedEvent.setSequenceNumber(sequenceNumber);
					}
				}
				if (queuedEventLE == null)
				{
					queuedEventLE = new QueuedEvent(eventObject, dispatchTime, sequenceNumber);
				}
				eventQueue.pushLast(queuedEventLE);
			}
			eventQueue.notifyAll();
		}
	}

	@Override
	public List<IEventItem> getEvents(long eventSequenceSince, long requestedMaximumWaitTime)
	{
		// take the requested amount of waitTime from the client with
		// the upper boundary 'MaxResponseDelay'
		long maximumWaitTime = maxResponseDelay < requestedMaximumWaitTime ? maxResponseDelay : requestedMaximumWaitTime;
		ArrayList<IQueuedEvent> selectedEvents = new ArrayList<IQueuedEvent>();
		long startedTime = System.currentTimeMillis();
		synchronized (eventQueue)
		{
			try
			{
				selectEvents(startedTime, eventSequenceSince, maximumWaitTime, selectedEvents);
			}
			finally
			{
				checkEventHistoryForCleanupIntern();
			}
		}
		if (selectedEvents.size() == 0)
		{
			return Collections.emptyList();
		}
		postLoadFromStore(selectedEvents);
		return batchAndConvertEvents(selectedEvents);
	}

	protected Object preSaveToStore(Object eventObject)
	{
		Class<?> eventType;
		if (eventObject instanceof IReplacedEvent)
		{
			eventType = ((IReplacedEvent) eventObject).getOriginalEventType();
		}
		else
		{
			eventType = eventObject.getClass();
		}
		IEventStoreHandler eventStoreHandler = eventStoreHandlers.getExtension(eventType);
		if (eventStoreHandler == null)
		{
			return eventObject;
		}
		// Replace object if necessary
		Object replacedEventObject = eventStoreHandler.preSaveInStore(eventObject);
		if (replacedEventObject == null || replacedEventObject == eventObject)
		{
			// Nothing to do
			return eventObject;
		}
		return replacedEventObject;
	}

	protected void postLoadFromStore(List<IQueuedEvent> selectedEvents)
	{
		for (int a = selectedEvents.size(); a-- > 0;)
		{
			IQueuedEvent selectedEvent = selectedEvents.get(a);
			Object eventObject = selectedEvent.getEventObject();
			Class<?> eventType;
			if (eventObject instanceof IReplacedEvent)
			{
				eventType = ((IReplacedEvent) eventObject).getOriginalEventType();
			}
			else
			{
				eventType = eventObject.getClass();
			}
			IEventStoreHandler eventStoreHandler = eventStoreHandlers.getExtension(eventType);
			if (eventStoreHandler == null)
			{
				continue;
			}
			// Replace object if necessary
			Object replacedEventObject = eventStoreHandler.postLoadFromStore(eventObject);
			if (replacedEventObject == null || replacedEventObject == eventObject)
			{
				// Nothing to do
				continue;
			}
			selectedEvents.set(a, new QueuedEvent(replacedEventObject, selectedEvent.getDispatchTime(), selectedEvent.getSequenceNumber()));
		}
	}

	protected List<IEventItem> batchAndConvertEvents(List<IQueuedEvent> selectedEvents)
	{
		IList<IQueuedEvent> batchedEvents = eventBatcher.batchEvents(selectedEvents);
		java.util.ArrayList<IEventItem> convertedEvents = new java.util.ArrayList<IEventItem>(batchedEvents.size());
		for (int a = 0, size = batchedEvents.size(); a < size; a++)
		{
			IQueuedEvent batchedEvent = batchedEvents.get(a);
			convertedEvents.add(new EventItem(batchedEvent.getEventObject(), batchedEvent.getDispatchTime(), batchedEvent.getSequenceNumber()));
		}
		return convertedEvents;
	}

	protected void selectEvents(long startedTime, long eventSequenceSince, long maximumWaitTime, List<IQueuedEvent> selectedEvents)
	{
		while (true)
		{
			IListElem<IQueuedEvent> currentLE = eventQueue.last();
			IListElem<IQueuedEvent> startLE = null;
			while (currentLE != null)
			{
				IQueuedEvent eventItem = currentLE.getElemValue();
				if (eventItem.getSequenceNumber() <= eventSequenceSince)
				{
					// This event is now older than the events we are interested in
					// Since all events are ordered we can go one stop further
					break;
				}
				startLE = currentLE;
				currentLE = currentLE.getPrev();
			}
			if (startLE != null)
			{
				currentLE = startLE;
				while (currentLE != null)
				{
					IQueuedEvent eventItem = currentLE.getElemValue();
					selectedEvents.add(eventItem);
					currentLE = currentLE.getNext();
				}
				return;
			}
			long passedTime = System.currentTimeMillis() - startedTime;
			long waitTimeSpan = maximumWaitTime - passedTime;
			if (waitTimeSpan <= 0)
			{
				return;
			}
			synchronized (eventQueue)
			{
				try
				{
					eventQueue.wait(waitTimeSpan);
				}
				catch (InterruptedException e)
				{
					// Intended blank
				}
				eventQueue.notifyAll();
			}
		}
	}

	@Override
	public long getCurrentEventSequence()
	{
		return eventSequence;
	}

	@Override
	public long findEventSequenceNumber(long time)
	{
		long requestedSequenceNumber = 0;

		IListElem<IQueuedEvent> currentLE = eventQueue.last();
		while (currentLE != null)
		{
			IQueuedEvent eventItem = currentLE.getElemValue();
			if (eventItem.getDispatchTime() < time)
			{
				// This event is now older than the event sequence number we are interested in
				// Since all events are ordered we can go one stop further
				requestedSequenceNumber = eventItem.getSequenceNumber();
				break;
			}
			currentLE = currentLE.getPrev();
		}

		return requestedSequenceNumber;
	}

	public void checkEventHistoryForCleanup()
	{
		synchronized (eventQueue)
		{
			checkEventHistoryForCleanupIntern();
			eventQueue.notifyAll();
		}
	}

	protected void checkEventHistoryForCleanupIntern()
	{
		long now = System.currentTimeMillis();
		if (now - lastCleanup < minCleanupDelay)
		{
			return;
		}
		lastCleanup = now;
		long timeToDelete = now - getMaxEventHistoryTime();
		IListElem<IQueuedEvent> currentLE = eventQueue.first();
		while (currentLE != null)
		{
			IQueuedEvent eventItem = currentLE.getElemValue();

			// Store next pointer as first thing
			IListElem<IQueuedEvent> nextLE = currentLE.getNext();

			if (eventItem.getDispatchTime() >= timeToDelete)
			{
				// Event is not old enough to get killed, but the queue is
				// sorted monotone increasing so we can break here
				break;
			}
			eventQueue.remove(currentLE);
			currentLE = nextLE;
			Object eventObject = eventItem.getEventObject();
			Class<?> eventType;
			if (eventObject instanceof IReplacedEvent)
			{
				eventType = ((IReplacedEvent) eventObject).getOriginalEventType();
			}
			else
			{
				eventType = eventObject.getClass();
			}
			IEventStoreHandler eventStoreHandler = eventStoreHandlers.getExtension(eventType);
			if (eventStoreHandler == null)
			{
				continue;
			}
			eventStoreHandler.eventRemovedFromStore(eventObject);
		}
	}

	@Override
	public void registerEventStoreHandler(IEventStoreHandler eventStoreHandler, Class<?> eventType)
	{
		eventStoreHandlers.register(eventStoreHandler, eventType);
	}

	@Override
	public void unregisterEventStoreHandler(IEventStoreHandler eventStoreHandler, Class<?> eventType)
	{
		eventStoreHandlers.unregister(eventStoreHandler, eventType);
	}
}
