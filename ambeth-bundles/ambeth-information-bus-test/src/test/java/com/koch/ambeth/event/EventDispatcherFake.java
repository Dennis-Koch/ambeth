package com.koch.ambeth.event;

/*-
 * #%L
 * jambeth-event-test
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

import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.state.IStateRollback;

import java.util.ArrayList;
import java.util.List;

public class EventDispatcherFake implements IEventDispatcher {
    public final List<DispatchedEvent> dispatchedEvents = new ArrayList<>();

    @Override
    public boolean isDispatchingBatchedEvents() {
        return false;
    }

    @Override
    public void dispatchEvent(Object eventObject) {
        dispatchEvent(eventObject, System.currentTimeMillis(), -1);
    }

    @Override
    public void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId) {
        dispatchedEvents.add(new DispatchedEvent(eventObject, dispatchTime, sequenceId));
    }

    @Override
    public boolean hasListeners(Class<?> eventType) {
        return true;
    }

    @Override
    public void waitEventToResume(Object eventTargetToResume, long maxWaitTime, CheckedConsumer<IProcessResumeItem> resumeDelegate, CheckedConsumer<Throwable> errorDelegate) {
        CheckedConsumer.invoke(resumeDelegate, null);
    }

    @Override
    public void enableEventQueue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushEventQueue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateRollback pause(Object eventTarget) {
        throw new UnsupportedOperationException();
    }

    class DispatchedEvent {
        public Object eventObject;
        public long dispatchTime;
        public long sequenceId;

        public DispatchedEvent(Object eventObject, long dispatchTime, long sequenceId) {
            this.eventObject = eventObject;
            this.dispatchTime = dispatchTime;
            this.sequenceId = sequenceId;
        }
    }
}
