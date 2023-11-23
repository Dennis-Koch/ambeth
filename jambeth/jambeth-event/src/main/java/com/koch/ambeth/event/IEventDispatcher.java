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

import com.koch.ambeth.util.function.CheckedConsumer;

public interface IEventDispatcher extends IEventQueue {
    void dispatchEvent(Object eventObject);

    void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId);

    /**
     * Checks for listeners for a given event type. This method should be used before constructing
     * expensive events objects. Do not bother if you already have all data for your event and just
     * have to create the event object.
     *
     * @param eventType Type to check for listeners
     * @return <code>true</code> iff there are listeners for the event type, <code>false</code>
     * otherwise
     */
    boolean hasListeners(Class<?> eventType);

    void waitEventToResume(Object eventTargetToResume, long maxWaitTime, CheckedConsumer<IProcessResumeItem> resumeDelegate, CheckedConsumer<Throwable> errorDelegate);
}
