using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Event
{
    public class EventListenerRegistry : IEventListenerExtendable, IEventTargetListenerExtendable, IEventBatcherExtendable, IEventTargetExtractorExtendable, IEventBatcher, IEventDispatcher, IEventListener, IEventQueue
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly HashMap<Type, List<IEventListenerMarker>> typeToListenersDict = new HashMap<Type, List<IEventListenerMarker>>();

        protected readonly IMapExtendableContainer<Type, IEventBatcher> typeToBatchersDict = new ClassExtendableContainer<IEventBatcher>("eventBatcher", "eventType");

        protected readonly IMapExtendableContainer<Type, IEventTargetExtractor> typeToEventTargetExtractorsDict = new ClassExtendableContainer<IEventTargetExtractor>("eventTargetExtractor", "eventType");

        protected readonly IList<IEventListenerMarker> globalListenerList = new List<IEventListenerMarker>();

	    protected readonly ThreadLocal<IList<IList<IQueuedEvent>>> eventQueueTL = new ThreadLocal<IList<IList<IQueuedEvent>>>();

        protected readonly IdentityLinkedSet<WaitForResumeItem> waitForResumeSet = new IdentityLinkedSet<WaitForResumeItem>();

        protected readonly IDictionary<PausedEventTargetItem, PausedEventTargetItem> pausedTargetsSet = new Dictionary<PausedEventTargetItem, PausedEventTargetItem>();

        protected readonly Lock readLock, writeLock, listenersReadLock, listenersWriteLock;

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

	    public EventListenerRegistry()
	    {
            ReadWriteLock rwLock = new ReadWriteLock();
		    readLock = rwLock.ReadLock;
		    writeLock = rwLock.WriteLock;
            ReadWriteLock listenersRwLock = new ReadWriteLock();
            listenersReadLock = listenersRwLock.ReadLock;
            listenersWriteLock = listenersRwLock.WriteLock;
	    }
        
	    public void EnableEventQueue()
	    {
		    IList<IList<IQueuedEvent>> eventQueueList = eventQueueTL.Value;
		    if (eventQueueList == null)
		    {
                eventQueueList = new List<IList<IQueuedEvent>>();
			    eventQueueTL.Value = eventQueueList;
		    }
		    IList<IQueuedEvent> eventQueue = new List<IQueuedEvent>();
		    eventQueueList.Add(eventQueue);
	    }

	    public void FlushEventQueue()
	    {
            IList<IList<IQueuedEvent>> eventQueueList = eventQueueTL.Value;
		    if (eventQueueList == null)
		    {
			    return;
		    }
            IList<IQueuedEvent> eventQueue = eventQueueList[eventQueueList.Count - 1];
		    eventQueueList.RemoveAt(eventQueueList.Count - 1);
		    if (eventQueueList.Count == 0)
		    {
			    eventQueueTL.Value = null;
			    eventQueueList = null;
		    }
			if (eventQueueList != null)
			{
				// Current flush is not the top-most flush. So we have to re-insert the events
				// One level out of our current level. We maintain the order at which the events have been queued
				IList<IQueuedEvent> outerEventQueue = eventQueueList[eventQueueList.Count - 1];
				for (int a = 0, size = eventQueue.Count; a < size; a++)
				{
                    IQueuedEvent queuedEvent = eventQueue[a];
					outerEventQueue.Add(queuedEvent);
				}
				return;
			}
            IList<IQueuedEvent> batchedEvents = BatchEvents(eventQueue);
            for (int a = 0, size = batchedEvents.Count; a < size; a++)
            {
                IQueuedEvent batchedEvent = batchedEvents[a];
                HandleEvent(batchedEvent.EventObject, batchedEvent.DispatchTime, batchedEvent.SequenceNumber);
            }
        }

        public IList<IQueuedEvent> BatchEvents(IList<IQueuedEvent> eventItems)
	    {
            List<IQueuedEvent> outputEvents;
		    if (eventItems.Count == 0)
		    {
                outputEvents = new List<IQueuedEvent>(0);
			    return outputEvents;
		    }
		    if (eventItems.Count == 1)
		    {
                outputEvents = new List<IQueuedEvent>(1);
                IQueuedEvent soleEvent = eventItems[0];
			    outputEvents.Add(soleEvent);
			    return outputEvents;
		    }
		    outputEvents = new List<IQueuedEvent>();
            IList<IQueuedEvent> currentBatchableEvents = new List<IQueuedEvent>();

            IList<IQueuedEvent> batchedEvents;
		    IEventBatcher currentEventBatcher = null;
		    for (int i = 0, size = eventItems.Count; i < size; i++)
		    {
                IQueuedEvent queuedEvent = eventItems[i];
			    Object eventObject = queuedEvent.EventObject;
			    IEventBatcher eventBatcher = typeToBatchersDict.GetExtension(eventObject.GetType());
			    if (eventBatcher != null)
			    {
				    if (currentEventBatcher != null && !eventBatcher.Equals(currentEventBatcher))
				    {
					    batchedEvents = BatchEventsIntern(currentBatchableEvents, currentEventBatcher);
					    if (batchedEvents != null)
					    {
						    outputEvents.AddRange(batchedEvents);
					    }
					    currentBatchableEvents.Clear();
				    }
				    currentEventBatcher = eventBatcher;
				    currentBatchableEvents.Add(queuedEvent);
				    continue;
			    }
			    batchedEvents = BatchEventsIntern(currentBatchableEvents, currentEventBatcher);
			    if (batchedEvents != null)
			    {
				    outputEvents.AddRange(batchedEvents);
			    }
			    currentBatchableEvents.Clear();
			    currentEventBatcher = null;
			    outputEvents.Add(queuedEvent);
		    }
		    batchedEvents = BatchEventsIntern(currentBatchableEvents, currentEventBatcher);
		    if (batchedEvents != null)
		    {
			    outputEvents.AddRange(batchedEvents);
		    }
		    return outputEvents;
	    }

        protected IList<IQueuedEvent> BatchEventsIntern(IList<IQueuedEvent> currentBatchableEvents, IEventBatcher currentEventBatcher)
        {
            if (currentBatchableEvents.Count == 0)
            {
                return null;
            }
            if (currentBatchableEvents.Count == 1 || currentEventBatcher == null)
            {
                List<IQueuedEvent> batchedEvents = new List<IQueuedEvent>(currentBatchableEvents.Count);
                batchedEvents.AddRange(currentBatchableEvents);
                return batchedEvents;
            }
            else
            {
                return currentEventBatcher.BatchEvents(currentBatchableEvents);
            }
        }

        public void DispatchEvent(Object eventObject)
        {
            DispatchEvent(eventObject, DateTime.Now, -1);
        }

        public void DispatchEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
        {
            HandleEvent(eventObject, dispatchTime, sequenceId);
        }

        public void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
        {
            if (eventObject == null)
            {
                return;
            }
            IList<IList<IQueuedEvent>> tlEventQueueList = eventQueueTL.Value;
            if (tlEventQueueList != null)
            {
                IList<IQueuedEvent> tlEventQueue = tlEventQueueList[tlEventQueueList.Count - 1];
                tlEventQueue.Add(new QueuedEvent(eventObject, dispatchTime, sequenceId));
                return;
            }
            IdentityLinkedSet<IEventListenerMarker> interestedEventListeners;
            IList<Object> pausedEventTargets;
            Lock listenersReadLock = this.listenersReadLock;
            listenersReadLock.Lock();
            try
            {
                interestedEventListeners = new IdentityLinkedSet<IEventListenerMarker>();
                interestedEventListeners.AddAll(globalListenerList); // Copy all global listeners as interested

                pausedEventTargets = EvaluatePausedEventTargets();

                Type currentType = eventObject.GetType();
                while (currentType != null)
                {
                    EvaluateType(currentType, interestedEventListeners);
                    Type[] interfaces = currentType.GetInterfaces();
                    foreach (Type typeInterface in interfaces)
                    {
                        EvaluateType(typeInterface, interestedEventListeners);
                    }
                    currentType = currentType.BaseType;
                }
            }
            finally
            {
                listenersReadLock.Unlock();
            }
            foreach (IEventListenerMarker interestedEventListener in interestedEventListeners)
            {
                NotifyEventListener(interestedEventListener, eventObject, null, pausedEventTargets, dispatchTime, sequenceId);
            }
        }

        protected IList<Object> EvaluatePausedEventTargets()
        {
            ICollection<PausedEventTargetItem> keys = pausedTargetsSet.Keys;
            List<Object> pausedEventTargets = new List<Object>(keys.Count);
            foreach (PausedEventTargetItem pauseETI in keys)
            {
                pausedEventTargets.Add(pauseETI.EventTarget);
            }
            return pausedEventTargets;
        }

        protected void EvaluateType(Type type, IISet<IEventListenerMarker> interestedEventListeners)
        {
            List<IEventListenerMarker> eventTypeListeners = typeToListenersDict.Get(type);

            if (eventTypeListeners != null)
            {
                interestedEventListeners.AddAll(eventTypeListeners);
            }
        }

        protected void EvaluateTypeForEventTarget(Type type, ISet<IEventTargetEventListener> interestedEventListeners)
	    {
		    List<IEventListenerMarker> eventTypeListeners = typeToListenersDict.Get(type);
		    if (eventTypeListeners == null)
		    {
                return;
            }
			for (int a = 0, size = eventTypeListeners.Count; a < size; a++)
			{
				IEventListenerMarker eventListener = eventTypeListeners[a];
				if (eventListener is IEventTargetEventListener)
				{
					interestedEventListeners.Add((IEventTargetEventListener) eventListener);
				}
			}
	    }

        protected void NotifyEventListener(IEventListenerMarker eventListener, Object eventObject, Object eventTarget, IList<Object> pausedEventTargets,
            DateTime dispatchTime, long sequenceId)
        {
            try
            {
                if (eventListener is IEventTargetEventListener)
			    {
                    ((IEventTargetEventListener)eventListener).HandleEvent(eventObject, eventTarget, pausedEventTargets, dispatchTime, sequenceId);
			    }
			    else if (eventListener is IEventListener)
			    {
				    ((IEventListener) eventListener).HandleEvent(eventObject, dispatchTime, sequenceId);
			    }
            }
            catch (System.Exception e)
            {
                if (Log.ErrorEnabled)
                {
                    Log.Error(e);
                }
            }
        }

	    public void RegisterEventListener(IEventListener eventListener)
	    {
		    RegisterEventListenerIntern(eventListener, null);
	    }

	    public void RegisterEventTargetListener(IEventTargetEventListener eventTargetListener)
	    {
		    RegisterEventListenerIntern(eventTargetListener, null);
	    }

	    public void RegisterEventListener(IEventListener eventListener, Type eventType)
	    {
		    RegisterEventListenerIntern(eventListener, eventType);
	    }

	    public void RegisterEventTargetListener(IEventTargetEventListener eventTargetListener, Type eventType)
	    {
		    RegisterEventListenerIntern(eventTargetListener, eventType);
	    }

        protected void RegisterEventListenerIntern(IEventListenerMarker eventListener, Type eventType)
	    {
            if (eventListener == null)
            {
                throw new ArgumentException("Argument must not be null", "eventListener");
            }
            listenersWriteLock.Lock();
            try
            {
                bool success = true;
                try
                {
                    if (eventType == null)
                    {
                        if (globalListenerList.Contains(eventListener))
                        {
                            throw new ArgumentException("Given eventListener already registered with this type");
                        }
                        globalListenerList.Add(eventListener);
                    }
                    else
                    {
                        List<IEventListenerMarker> eventListeners = typeToListenersDict.Get(eventType);
                        if (eventListeners == null)
                        {
                            eventListeners = new List<IEventListenerMarker>();
                            typeToListenersDict.Put(eventType, eventListeners);
                        }
                        if (eventListeners.Contains(eventListener))
                        {
                            throw new ArgumentException("Given eventListener already registered with this type");
                        }
                        eventListeners.Add(eventListener);
                    }
                    success = true;
                }
                finally
                {
                    if (!success)
                    {
                        UnregisterEventListenerForCleanup(eventListener);
                    }
                }
            }
            finally
            {
                listenersWriteLock.Unlock();
            }
        }

	    public void UnregisterEventListener(IEventListener eventListener)
	    {
		    UnregisterEventListenerIntern(eventListener, null);
	    }

        public void UnregisterEventTargetListener(IEventTargetEventListener eventTargetListener)
	    {
		    UnregisterEventListenerIntern(eventTargetListener, null);
	    }

        public void UnregisterEventListener(IEventListener eventListener, Type eventType)
	    {
		    UnregisterEventListenerIntern(eventListener, eventType);
	    }

	    public void UnregisterEventTargetListener(IEventTargetEventListener eventTargetListener, Type eventType)
	    {
		    UnregisterEventListenerIntern(eventTargetListener, eventType);
	    }

	    protected void UnregisterEventListenerIntern(IEventListenerMarker eventListener, Type eventType)
	    {
            if (eventListener == null)
            {
                throw new ArgumentException("Argument must not be null", "eventListener");
            }
            listenersWriteLock.Lock();
            try
            {
                bool success = false;
                try
                {
                    if (eventType == null)
                    {
                        if (!globalListenerList.Remove(eventListener))
                        {
                            throw new ArgumentException("Given eventListener is not registered to this type");
                        }
                    }
                    else
                    {
                        List<IEventListenerMarker> eventListeners = typeToListenersDict.Get(eventType);
                        if (eventListeners == null || !eventListeners.Remove(eventListener))
                        {
                            throw new ArgumentException("Given dataChangeListener is not registered to this type");
                        }
                        if (eventListeners != null && eventListeners.Count == 0)
                        {
                            typeToListenersDict.Remove(eventType);
                        }
                    }
                    success = true;
                }
                finally
                {
                    if (!success)
                    {
                        UnregisterEventListenerForCleanup(eventListener);
                    }
                }
            }
            finally
            {
                listenersWriteLock.Unlock();
            }
        }

        protected void UnregisterEventListenerForCleanup(IEventListenerMarker eventListener)
        {
            globalListenerList.Remove(eventListener);
            foreach (Entry<Type, IList<IEventListenerMarker>> entry in typeToListenersDict)
            {
                IList<IEventListenerMarker> eventListeners = entry.Value;
                if (eventListeners != null)
                {
                    eventListeners.Remove(eventListener);
                }
            }
        }

        public void RegisterEventBatcher(IEventBatcher eventBatcher, Type eventType)
        {
            typeToBatchersDict.Register(eventBatcher, eventType);
        }

        public void UnregisterEventBatcher(IEventBatcher eventBatcher, Type eventType)
        {
            typeToBatchersDict.Unregister(eventBatcher, eventType);
        }

        public T InvokeWithoutLocks<T>(IResultingBackgroundWorkerDelegate<T> run)
        {
            LockState lockState = writeLock.ReleaseAllLocks();
            try
            {
                return run.Invoke();
            }
            finally
            {
                writeLock.ReacquireLocks(lockState);
            }
        }

        public void Pause(Object eventTarget)
        {
            if (GuiThreadHelper.IsInGuiThread())
            {
                // Nothing to do
                return;
            }
            IEventTargetExtractor eventTargetExtractor = typeToEventTargetExtractorsDict.GetExtension(eventTarget.GetType());
            if (eventTargetExtractor == null)
            {
                return;
            }
            eventTarget = eventTargetExtractor.ExtractEventTarget(eventTarget);
		    if (eventTarget == null)
		    {
			    return;
		    }
		    PausedEventTargetItem etiKey = new PausedEventTargetItem(eventTarget);
		    listenersWriteLock.Lock();
		    try
		    {
			    PausedEventTargetItem pauseETI = DictionaryExtension.ValueOrDefault(pausedTargetsSet, etiKey);
			    if (pauseETI == null)
			    {
				    pauseETI = etiKey;
				    pausedTargetsSet.Add(pauseETI, pauseETI);
			    }
			    pauseETI.PauseCount++;
		    }
		    finally
		    {
			    listenersWriteLock.Unlock();
		    }
        }

        public void Resume(Object eventTarget)
        {
            if (GuiThreadHelper.IsInGuiThread())
            {
                // Nothing to do
                return;
            }
            IEventTargetExtractor eventTargetExtractor = typeToEventTargetExtractorsDict.GetExtension(eventTarget.GetType());
            if (eventTargetExtractor == null)
            {
                return;
            }
            eventTarget = eventTargetExtractor.ExtractEventTarget(eventTarget);
            if (eventTarget == null)
            {
                return;
            }
            IdentityLinkedSet<WaitForResumeItem> freeLatchMap = null;
            try
            {
                PausedEventTargetItem etiKey = new PausedEventTargetItem(eventTarget);
                PausedEventTargetItem pauseETI;
                listenersWriteLock.Lock();
                try
                {
                    pauseETI = DictionaryExtension.ValueOrDefault(pausedTargetsSet, etiKey);
                    if (pauseETI == null)
                    {
                        throw new System.Exception("No pause() active for target " + eventTarget);
                    }
                    pauseETI.PauseCount--;
                    if (pauseETI.PauseCount > 0)
                    {
                        return;
                    }
                    pausedTargetsSet.Remove(etiKey);

                    IList<Object> remainingPausedEventTargets = EvaluatePausedEventTargets();
                    IdentityHashSet<Object> remainingPausedEventTargetsSet = new IdentityHashSet<Object>();
                    Iterator<WaitForResumeItem> iter = waitForResumeSet.Iterator();
                    while (iter.MoveNext())
                    {
                        WaitForResumeItem pauseItem = iter.Current;
                        remainingPausedEventTargetsSet.AddAll(remainingPausedEventTargets);
                        remainingPausedEventTargetsSet.RetainAll(pauseItem.PendingPauses);

                        if (remainingPausedEventTargetsSet.Count == 0)
                        {
                            iter.Remove();
                            if (freeLatchMap == null)
                            {
                                freeLatchMap = new IdentityLinkedSet<WaitForResumeItem>();
                            }
                            freeLatchMap.Add(pauseItem);
                        }
                        remainingPausedEventTargetsSet.Clear();
                    }
                }
                finally
                {
                    listenersWriteLock.Unlock();
                }
            }
            finally
            {
                if (freeLatchMap != null)
                {
                    foreach (WaitForResumeItem wfrItem in freeLatchMap)
                    {
                        wfrItem.Latch.CountDown();
                    }
                    foreach (WaitForResumeItem wfrItem in freeLatchMap)
                    {
                        try
                        {
                            wfrItem.ResultLatch.Await();
                        }
                        catch (System.Exception e)
                        {
                            throw new System.Exception("Fatal state occured. This may result in a global deadlock", e);
                        }
                    }
                }
            }
        }

        public void WaitEventToResume(Object eventTargetToResume, long maxWaitTime, IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate, IBackgroundWorkerParamDelegate<Exception> errorDelegate)
	    {
            try
            {
                IdentityHashSet<Object> pendingSet = new IdentityHashSet<Object>();
                if (eventTargetToResume is IEnumerable)
                {
                    pendingSet.AddAll((IEnumerable)eventTargetToResume);
                }
                else
                {
                    pendingSet.Add(eventTargetToResume);
                }
                WaitForResumeItem pauseItem = null;
                listenersWriteLock.Lock();
                try
                {
                    IList<Object> remainingPausedEventTargets = EvaluatePausedEventTargets();
                    IdentityLinkedSet<Object> remainingPausedEventTargetsSet = new IdentityLinkedSet<Object>(remainingPausedEventTargets);
                    remainingPausedEventTargetsSet.RetainAll(pendingSet);

                    if (remainingPausedEventTargetsSet.Count > 0)
                    {
                        // We should wait now but we have to check if we are in the UI thread, which must never wait
                        if (GuiThreadHelper.IsInGuiThread())
                        {
							// This is the trick: We "requeue" the current action in the UI pipeline to prohibit blocking
                            GuiThreadHelper.InvokeInGuiLate(delegate()
                            {
                                WaitEventToResume(eventTargetToResume, maxWaitTime, resumeDelegate, errorDelegate);
                            });
                            return;
                        }
                        pauseItem = new WaitForResumeItem(pendingSet);
                        waitForResumeSet.Add(pauseItem);
                    }
                }
                finally
                {
                    listenersWriteLock.Unlock();
                }
                if (pauseItem == null)
                {
                    resumeDelegate.Invoke(null);
                    return;
                }
                CountDownLatch latch = pauseItem.Latch;
                if (maxWaitTime < 0)
                {
                    latch.Await();
                }
                else if (maxWaitTime > 0)
                {
                    latch.Await(TimeSpan.FromMilliseconds(maxWaitTime));
                }
                else
                {
                    throw new System.Exception("Thread should wait but does not want to");
                }
                resumeDelegate.Invoke(pauseItem);
            }
            catch (Exception e)
            {
                if (Log.ErrorEnabled)
                {
                    Log.Error(e);
                }
                if (errorDelegate != null)
                {
                    errorDelegate.Invoke(e);
                }
                throw;
            }
	    }

        public void RegisterEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Type eventType)
	    {
		    typeToEventTargetExtractorsDict.Register(eventTargetExtractor, eventType);
	    }

	    public void UnregisterEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Type eventType)
	    {
		    typeToEventTargetExtractorsDict.Unregister(eventTargetExtractor, eventType);
	    }
    }
}
