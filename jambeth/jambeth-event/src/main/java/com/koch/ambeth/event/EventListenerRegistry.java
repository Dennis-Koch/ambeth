package com.koch.ambeth.event;

/*-
 * #%L
 * jambeth-event
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.extendable.ClassExtendableListContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class EventListenerRegistry
        implements IEventListenerExtendable, IEventTargetListenerExtendable, IEventBatcherExtendable, IEventTargetExtractorExtendable, IEventBatcher, IEventDispatcher, IEventListener, IEventQueue {
    protected final ClassExtendableListContainer<IEventListenerMarker> typeToListenersDict = new ClassExtendableListContainer<>("eventListener", "eventType");
    protected final ClassExtendableContainer<IEventBatcher> typeToBatchersDict = new ClassExtendableContainer<>("eventBatcher", "eventType");
    protected final ClassExtendableContainer<IEventTargetExtractor> typeToEventTargetExtractorsDict = new ClassExtendableContainer<>("eventTargetExtractor", "eventType");
    protected final ThreadLocal<IList<IList<IQueuedEvent>>> eventQueueTL = new SensitiveThreadLocal<>();
    protected final IdentityLinkedSet<WaitForResumeItem> waitForResumeSet = new IdentityLinkedSet<>();
    protected final IdentityLinkedMap<Object, PausedEventTargetItem> pausedTargets = new IdentityLinkedMap<>();
    protected final Lock listenersReadLock, listenersWriteLock;
    protected final ThreadLocal<Boolean> isDispatchingBatchedEventsTL = new ThreadLocal<>();
    @Autowired
    protected IGuiThreadHelper guiThreadHelper;
    @LogInstance
    private ILogger log;

    public EventListenerRegistry() {
        listenersReadLock = typeToListenersDict.getWriteLock();
        listenersWriteLock = typeToListenersDict.getWriteLock();
    }

    @Override
    public boolean isDispatchingBatchedEvents() {
        return Boolean.TRUE.equals(isDispatchingBatchedEventsTL.get());
    }

    @Override
    public void enableEventQueue() {
        IList<IList<IQueuedEvent>> eventQueueList = eventQueueTL.get();
        if (eventQueueList == null) {
            eventQueueList = new ArrayList<>();
            eventQueueTL.set(eventQueueList);
        }
        ArrayList<IQueuedEvent> eventQueue = new ArrayList<>();
        eventQueueList.add(eventQueue);
    }

    @Override
    public void flushEventQueue() {
        var eventQueueList = eventQueueTL.get();
        if (eventQueueList == null) {
            return;
        }
        var eventQueue = eventQueueList.remove(eventQueueList.size() - 1);
        if (eventQueueList.isEmpty()) {
            eventQueueTL.remove();
            eventQueueList = null;
        }
        if (eventQueueList != null) {
            // Current flush is not the top-most flush. So we have to re-insert the events
            // One level out of our current level. We maintain the order at which the events have been
            // queued
            var outerEventQueue = eventQueueList.get(eventQueueList.size() - 1);
            for (int a = 0, size = eventQueue.size(); a < size; a++) {
                var queuedEvent = eventQueue.get(a);
                outerEventQueue.add(queuedEvent);
            }
            return;
        }
        var batchedEvents = batchEvents(eventQueue);
        var collectedBatchedEventDispatchAwareSet = new IdentityLinkedSet<IBatchedEventListener>();

        var tlEventQueueList = eventQueueTL.get();

        var oldDispatchingBatchedEvents = isDispatchingBatchedEventsTL.get();
        isDispatchingBatchedEventsTL.set(Boolean.TRUE);
        try {
            for (int a = 0, size = batchedEvents.size(); a < size; a++) {
                var batchedEvent = batchedEvents.get(a);
                if (tlEventQueueList != null) {
                    var tlEventQueue = tlEventQueueList.get(tlEventQueueList.size() - 1);
                    tlEventQueue.add(new QueuedEvent(batchedEvent.getEventObject(), batchedEvent.getDispatchTime(), batchedEvent.getSequenceNumber()));
                    continue;
                }
                handleEventIntern(batchedEvent.getEventObject(), batchedEvent.getDispatchTime(), batchedEvent.getSequenceNumber(), collectedBatchedEventDispatchAwareSet);
            }
        } finally {
            isDispatchingBatchedEventsTL.set(oldDispatchingBatchedEvents);
        }
        for (var eventListener : collectedBatchedEventDispatchAwareSet) {
            eventListener.flushBatchedEventDispatching();
        }
    }

    @Override
    public IList<IQueuedEvent> batchEvents(List<IQueuedEvent> eventItems) {
        if (eventItems.isEmpty()) {
            return EmptyList.<IQueuedEvent>getInstance();
        }
        if (eventItems.size() == 1) {
            var soleEvent = eventItems.get(0);
            var outputEvents = new ArrayList<IQueuedEvent>(1);
            outputEvents.add(soleEvent);
            return outputEvents;
        }
        var currentBatchableEvents = new ArrayList<IQueuedEvent>();

        var outputEvents = new ArrayList<IQueuedEvent>(1);
        IEventBatcher currentEventBatcher = null;
        for (int i = 0, size = eventItems.size(); i < size; i++) {
            var queuedEvent = eventItems.get(i);
            var eventObject = queuedEvent.getEventObject();
            var eventBatcher = typeToBatchersDict.getExtension(eventObject.getClass());
            if (eventBatcher != null) {
                if (currentEventBatcher != null && !eventBatcher.equals(currentEventBatcher)) {
                    outputEvents.addAll(batchEventsIntern(currentBatchableEvents, currentEventBatcher));
                    currentBatchableEvents.clear();
                }
                currentEventBatcher = eventBatcher;
                currentBatchableEvents.add(queuedEvent);
                continue;
            }
            outputEvents.addAll(batchEventsIntern(currentBatchableEvents, currentEventBatcher));
            currentBatchableEvents.clear();
            currentEventBatcher = null;
            outputEvents.add(queuedEvent);
        }
        outputEvents.addAll(batchEventsIntern(currentBatchableEvents, currentEventBatcher));
        return outputEvents;
    }

    protected IList<IQueuedEvent> batchEventsIntern(IList<IQueuedEvent> currentBatchableEvents, IEventBatcher currentEventBatcher) {
        if (currentBatchableEvents.isEmpty()) {
            return EmptyList.<IQueuedEvent>getInstance();
        }
        if (currentBatchableEvents.size() == 1 || currentEventBatcher == null) {
            return currentBatchableEvents;
        } else {
            return currentEventBatcher.batchEvents(currentBatchableEvents);
        }
    }

    @Override
    public void dispatchEvent(Object eventObject) {
        dispatchEvent(eventObject, System.currentTimeMillis(), -1);
    }

    @Override
    public void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId) {
        handleEvent(eventObject, dispatchTime, sequenceId);
    }

    @Override
    public boolean hasListeners(Class<?> eventType) {
        IList<IEventListenerMarker> listeners = typeToListenersDict.getExtensions(eventType);
        return listeners != null && !listeners.isEmpty();
    }

    @Override
    public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
        if (eventObject == null) {
            return;
        }
        var tlEventQueueList = eventQueueTL.get();
        if (tlEventQueueList != null) {
            var tlEventQueue = tlEventQueueList.get(tlEventQueueList.size() - 1);
            tlEventQueue.add(new QueuedEvent(eventObject, dispatchTime, sequenceId));
            return;
        }
        handleEventIntern(eventObject, dispatchTime, sequenceId, null);
    }

    private void handleEventIntern(Object eventObject, long dispatchTime, long sequenceId, ISet<IBatchedEventListener> collectedBatchedEventDispatchAwareSet) {
        IList<IEventListenerMarker> interestedEventListeners;
        List<Object> pausedEventTargets;
        var listenersReadLock = this.listenersReadLock;
        listenersReadLock.lock();
        try {
            pausedEventTargets = evaluatePausedEventTargets();

            interestedEventListeners = typeToListenersDict.getExtensions(eventObject.getClass());
        } finally {
            listenersReadLock.unlock();
        }
        for (int a = 0, size = interestedEventListeners.size(); a < size; a++) {
            var eventListener = interestedEventListeners.get(a);
            if (collectedBatchedEventDispatchAwareSet != null && eventListener instanceof IBatchedEventListener && collectedBatchedEventDispatchAwareSet.add((IBatchedEventListener) eventListener)) {
                ((IBatchedEventListener) eventListener).enableBatchedEventDispatching();
            }
            try {
                if (eventListener instanceof IEventTargetEventListener) {
                    ((IEventTargetEventListener) eventListener).handleEvent(eventObject, null, pausedEventTargets, dispatchTime, sequenceId);
                } else if (eventListener instanceof IEventListener) {
                    ((IEventListener) eventListener).handleEvent(eventObject, dispatchTime, sequenceId);
                }
            } catch (Throwable e) {
                if (log.isErrorEnabled()) {
                    log.error(e);
                }
            }
        }
    }

    protected List<Object> evaluatePausedEventTargets() {
        var pausedTargets = this.pausedTargets;
        if (pausedTargets.isEmpty()) {
            return List.of();
        }
        var pausedEventTargets = new ArrayList<>(pausedTargets.size());
        for (var entry : pausedTargets) {
            var pauseETI = entry.getValue();
            pausedEventTargets.add(pauseETI.getEventTarget());
        }
        return pausedEventTargets;
    }

    protected List<Object> evaluatePausedEventTargetsOfForeignThreads() {
        var currentThread = Thread.currentThread();
        var pausedTargets = this.pausedTargets;
        var pausedEventTargets = new ArrayList<>(pausedTargets.size());
        for (var entry : pausedTargets) {
            var pauseETI = entry.getValue();
            if (pauseETI.getThread() != currentThread) {
                pausedEventTargets.add(pauseETI.getEventTarget());
            }
        }
        return pausedEventTargets;
    }

    protected void notifyEventListener(IEventListenerMarker eventListener, Object eventObject, Object eventTarget, List<Object> pausedEventTargets, long dispatchTime, long sequenceId) {
        try {
            if (eventListener instanceof IEventTargetEventListener) {
                ((IEventTargetEventListener) eventListener).handleEvent(eventObject, eventTarget, pausedEventTargets, dispatchTime, sequenceId);
            } else if (eventListener instanceof IEventListener) {
                ((IEventListener) eventListener).handleEvent(eventObject, dispatchTime, sequenceId);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e);
            }
        }
    }

    @Override
    public void registerEventListener(IEventListener eventListener) {
        registerEventListenerIntern(eventListener, null);
    }

    @Override
    public void registerEventTargetListener(IEventTargetEventListener eventTargetListener) {
        registerEventListenerIntern(eventTargetListener, null);
    }

    @Override
    public void registerEventListener(IEventListener eventListener, Class<?> eventType) {
        registerEventListenerIntern(eventListener, eventType);
    }

    @Override
    public void registerEventTargetListener(IEventTargetEventListener eventTargetListener, Class<?> eventType) {
        registerEventListenerIntern(eventTargetListener, eventType);
    }

    protected void registerEventListenerIntern(IEventListenerMarker eventListener, Class<?> eventType) {
        if (eventType == null) {
            eventType = Object.class;
        }
        typeToListenersDict.register(eventListener, eventType);
    }

    @Override
    public void unregisterEventListener(IEventListener eventListener) {
        unregisterEventListenerIntern(eventListener, null);
    }

    @Override
    public void unregisterEventTargetListener(IEventTargetEventListener eventTargetListener) {
        unregisterEventListenerIntern(eventTargetListener, null);
    }

    @Override
    public void unregisterEventListener(IEventListener eventListener, Class<?> eventType) {
        unregisterEventListenerIntern(eventListener, eventType);
    }

    @Override
    public void unregisterEventTargetListener(IEventTargetEventListener eventTargetListener, Class<?> eventType) {
        unregisterEventListenerIntern(eventTargetListener, eventType);
    }

    protected void unregisterEventListenerIntern(IEventListenerMarker eventListener, Class<?> eventType) {
        if (eventType == null) {
            eventType = Object.class;
        }
        typeToListenersDict.unregister(eventListener, eventType);
    }

    @Override
    public void registerEventBatcher(IEventBatcher eventBatcher, Class<?> eventType) {
        typeToBatchersDict.register(eventBatcher, eventType);
    }

    @Override
    public void unregisterEventBatcher(IEventBatcher eventBatcher, Class<?> eventType) {
        typeToBatchersDict.unregister(eventBatcher, eventType);
    }

    @Override
    public IStateRollback pause(Object eventTarget) {
        if (guiThreadHelper.isInGuiThread()) {
            // nothing to do
            return StateRollback.empty();
        }
        var eventTargetExtractor = typeToEventTargetExtractorsDict.getExtension(eventTarget.getClass());
        if (eventTargetExtractor != null) {
            eventTarget = eventTargetExtractor.extractEventTarget(eventTarget);
            if (eventTarget == null) {
                return StateRollback.empty();
            }
        }
        var listenersWriteLock = this.listenersWriteLock;
        listenersWriteLock.lock();
        try {
            var pauseETI = pausedTargets.get(eventTarget);
            if (pauseETI == null) {
                pauseETI = new PausedEventTargetItem(eventTarget);
                pausedTargets.put(eventTarget, pauseETI);
            }
            pauseETI.setPauseCount(pauseETI.getPauseCount() + 1);
            var fEventTarget = eventTarget;
            return () -> resume(fEventTarget);
        } finally {
            listenersWriteLock.unlock();
        }
    }

    protected void resume(Object eventTarget) {
        if (guiThreadHelper.isInGuiThread()) {
            // nothing to do
            return;
        }
        var eventTargetExtractor = typeToEventTargetExtractorsDict.getExtension(eventTarget.getClass());
        if (eventTargetExtractor != null) {
            eventTarget = eventTargetExtractor.extractEventTarget(eventTarget);
            if (eventTarget == null) {
                return;
            }
        }
        IdentityLinkedSet<WaitForResumeItem> freeLatchMap = null;
        try {
            PausedEventTargetItem pauseETI;
            var listenersWriteLock = this.listenersWriteLock;
            listenersWriteLock.lock();
            try {
                var pausedTargets = this.pausedTargets;
                pauseETI = pausedTargets.get(eventTarget);
                if (pauseETI == null) {
                    throw new IllegalStateException("No pause() active for target " + eventTarget);
                }
                pauseETI.setPauseCount(pauseETI.getPauseCount() - 1);
                if (pauseETI.getPauseCount() > 0) {
                    return;
                }
                pausedTargets.remove(eventTarget);
                if (!waitForResumeSet.isEmpty()) {
                    var remainingPausedEventTargets = evaluatePausedEventTargets();
                    var remainingPausedEventTargetsSet = IdentityLinkedSet.<Object>create(remainingPausedEventTargets.size());
                    var iter = waitForResumeSet.iterator();
                    while (iter.hasNext()) {
                        var pauseItem = iter.next();
                        remainingPausedEventTargetsSet.addAll(remainingPausedEventTargets);
                        remainingPausedEventTargetsSet.retainAll(pauseItem.pendingPauses);

                        if (remainingPausedEventTargetsSet.isEmpty()) {
                            iter.remove();
                            if (freeLatchMap == null) {
                                freeLatchMap = new IdentityLinkedSet<>();
                            }
                            freeLatchMap.add(pauseItem);
                        }
                        remainingPausedEventTargetsSet.clear();
                    }
                }
            } finally {
                listenersWriteLock.unlock();
            }
        } finally {
            if (freeLatchMap != null) {
                for (var resumeItem : freeLatchMap) {
                    resumeItem.getLatch().countDown();
                }
                for (var resumeItem : freeLatchMap) {
                    try {
                        while (!resumeItem.getResultLatch().await(1000, TimeUnit.MILLISECONDS)) {
                            log.info("RESUME AWAIT");
                        }
                    } catch (InterruptedException e) {
                        throw RuntimeExceptionUtil.mask(e, "Fatal state occured. This may result in a global deadlock");
                    }
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void waitEventToResume(final Object eventTargetToResume, final long maxWaitTime, final CheckedConsumer<IProcessResumeItem> resumeDelegate, final CheckedConsumer<Throwable> errorDelegate) {
        try {
            var pendingSet = new IdentityHashSet<>();
            if (eventTargetToResume instanceof Collection) {
                pendingSet.addAll((Collection<?>) eventTargetToResume);
            } else {
                pendingSet.add(eventTargetToResume);
            }
            WaitForResumeItem pauseItem = null;
            var listenersWriteLock = this.listenersWriteLock;
            listenersWriteLock.lock();
            try {
                var remainingPausedEventTargets = evaluatePausedEventTargetsOfForeignThreads();
                var remainingPausedEventTargetsSet = new IdentityLinkedSet<>(remainingPausedEventTargets);
                remainingPausedEventTargetsSet.retainAll(pendingSet);

                if (!remainingPausedEventTargetsSet.isEmpty()) {
                    // We should wait now but we have to check if we are in the UI thread, which must never
                    // wait
                    if (guiThreadHelper.isInGuiThread()) {
                        // This is the trick: We "requeue" the current action in the UI pipeline to prohibit
                        // blocking
                        guiThreadHelper.invokeInGuiLate(() -> waitEventToResume(eventTargetToResume, maxWaitTime, resumeDelegate, errorDelegate));
                        return;
                    }
                    pauseItem = new WaitForResumeItem(pendingSet);
                    waitForResumeSet.add(pauseItem);
                }
            } finally {
                listenersWriteLock.unlock();
            }
            if (pauseItem == null) {
                resumeDelegate.accept(null);
                return;
            }
            var latch = pauseItem.getLatch();
            try {
                if (maxWaitTime < 0) {
                    while (!latch.await(1000, TimeUnit.MILLISECONDS)) {
                        log.info("WAITING");
                    }
                } else if (maxWaitTime > 0) {
                    latch.await(maxWaitTime, TimeUnit.MILLISECONDS);
                } else {
                    throw new IllegalStateException("Thread should wait but does not want to");
                }
            } catch (InterruptedException e) {
                throw RuntimeExceptionUtil.mask(e);
            }
            resumeDelegate.accept(pauseItem);
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error(e);
            }
            if (errorDelegate != null) {
                errorDelegate.accept(e);
            }
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public void registerEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Class<?> eventType) {
        typeToEventTargetExtractorsDict.register(eventTargetExtractor, eventType);
    }

    @Override
    public void unregisterEventTargetExtractor(IEventTargetExtractor eventTargetExtractor, Class<?> eventType) {
        typeToEventTargetExtractorsDict.unregister(eventTargetExtractor, eventType);
    }
}
