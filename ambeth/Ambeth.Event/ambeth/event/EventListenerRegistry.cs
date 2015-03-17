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

        protected readonly ClassExtendableListContainer<IEventListenerMarker> typeToListenersDict = new ClassExtendableListContainer<IEventListenerMarker>("eventListener", "eventType");

        protected readonly ClassExtendableContainer<IEventBatcher> typeToBatchersDict = new ClassExtendableContainer<IEventBatcher>("eventBatcher", "eventType");

        protected readonly ClassExtendableContainer<IEventTargetExtractor> typeToEventTargetExtractorsDict = new ClassExtendableContainer<IEventTargetExtractor>("eventTargetExtractor", "eventType");

	    protected readonly ThreadLocal<IList<IList<IQueuedEvent>>> eventQueueTL = new ThreadLocal<IList<IList<IQueuedEvent>>>();

        protected readonly IdentityLinkedSet<WaitForResumeItem> waitForResumeSet = new IdentityLinkedSet<WaitForResumeItem>();

        protected readonly IdentityLinkedMap<Object, PausedEventTargetItem> pausedTargets = new IdentityLinkedMap<Object, PausedEventTargetItem>();

        protected readonly Lock listenersReadLock, listenersWriteLock;

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

	    public EventListenerRegistry()
	    {
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
            IList<IEventListenerMarker> interestedEventListeners;
            IList<Object> pausedEventTargets;
            Lock listenersReadLock = this.listenersReadLock;
            listenersReadLock.Lock();
            try
            {
                pausedEventTargets = EvaluatePausedEventTargets();

                interestedEventListeners = typeToListenersDict.GetExtensions(eventObject.GetType());
            }
            finally
            {
                listenersReadLock.Unlock();
            }
            for (int a = 0, size = interestedEventListeners.Count; a < size; a++)
            {
                NotifyEventListener(interestedEventListeners[a], eventObject, null, pausedEventTargets, dispatchTime, sequenceId);
            }
        }

        protected IList<Object> EvaluatePausedEventTargets()
	    {
            IdentityLinkedMap<Object, PausedEventTargetItem> pausedTargets = this.pausedTargets;
		    List<Object> pausedEventTargets = new List<Object>(pausedTargets.Count);
            foreach (Entry<Object, PausedEventTargetItem> entry in pausedTargets)
		    {
                PausedEventTargetItem pauseETI = entry.Value;
			    pausedEventTargets.Add(pauseETI.EventTarget);
		    }
		    return pausedEventTargets;
	    }

        protected IList<Object> EvaluatePausedEventTargetsOfForeignThreads()
	    {
		    Thread currentThread = Thread.CurrentThread;
            IdentityLinkedMap<Object, PausedEventTargetItem> pausedTargets = this.pausedTargets;
		    List<Object> pausedEventTargets = new List<Object>(pausedTargets.Count);
            foreach (Entry<Object, PausedEventTargetItem> entry in pausedTargets)
            {
                PausedEventTargetItem pauseETI = entry.Value;
			    if (!Object.ReferenceEquals(pauseETI.Thread, currentThread))
			    {
				    pausedEventTargets.Add(pauseETI.EventTarget);
			    }
		    }
		    return pausedEventTargets;
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
            if (eventType == null)
		    {
			    eventType = typeof(Object);
		    }
		    typeToListenersDict.Register(eventListener, eventType);
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
            if (eventType == null)
            {
                eventType = typeof(Object);
            }
            typeToListenersDict.Unregister(eventListener, eventType);
        }

        public void RegisterEventBatcher(IEventBatcher eventBatcher, Type eventType)
        {
            typeToBatchersDict.Register(eventBatcher, eventType);
        }

        public void UnregisterEventBatcher(IEventBatcher eventBatcher, Type eventType)
        {
            typeToBatchersDict.Unregister(eventBatcher, eventType);
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
			    PausedEventTargetItem pauseETI = pausedTargets.Get(eventTarget);
			    if (pauseETI == null)
			    {
                    pauseETI = new PausedEventTargetItem(eventTarget);
                    pausedTargets.Put(eventTarget, pauseETI);
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
                PausedEventTargetItem pauseETI;
                listenersWriteLock.Lock();
                try
                {
                    IdentityLinkedMap<Object, PausedEventTargetItem> pausedTargets = this.pausedTargets;
                    pauseETI = pausedTargets.Get(eventTarget);
                    if (pauseETI == null)
                    {
                        throw new System.Exception("No pause() active for target " + eventTarget);
                    }
                    pauseETI.PauseCount--;
                    if (pauseETI.PauseCount > 0)
                    {
                        return;
                    }
                    pausedTargets.Remove(eventTarget);

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
                    IList<Object> remainingPausedEventTargets = EvaluatePausedEventTargetsOfForeignThreads();
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
