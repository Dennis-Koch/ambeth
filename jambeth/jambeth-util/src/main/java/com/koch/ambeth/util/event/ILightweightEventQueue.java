package com.koch.ambeth.util.event;

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

public interface ILightweightEventQueue {
    /**
     * Used together with {@link #flushEventQueue()} to prepare bulk-dispatching operations. After
     * invoking this method all dispatching operations to
     * {@link com.koch.ambeth.event.IEventDispatcher#dispatchEvent(Object)} and its overloads are queued up and wait for a
     * {@link #flushEventQueue()}.<br>
     * </br>
     * This method stacks: Multiple "enable" calls need the same amount of "flush" calls till a real
     * flush is done and the right {@link com.koch.ambeth.event.IEventListener}s are notified. See also
     * {@link #flushEventQueue()} for more details about whats happening during the flush. It is
     * recommended to use it consistently in a try-finally statement:
     *
     * <pre>
     * <code>
     * eventQueue.enableEventQueue();
     * try
     * {
     *   // do stuff
     * }
     * finally
     * {
     *   eventQueue.flushEventQueue();
     * }
     * </code>
     * </pre>
     */
    void enableEventQueue();

    /**
     * Used together with {@link #enableEventQueue()} to prepare bulk-dispatching operations. If the
     * internal stack count (together with {@link #enableEventQueue()}) reaches zero the queued events
     * are "batched" together: This means some optional compact algorithm may process the events -
     * e.g. to reduce potential redundancy of events which somehow "outdate" previous events. After
     * the batch sequence the remaining (or newly created, compacted) events get dispatched to the
     * registered {@link com.koch.ambeth.event.IEventListener}s.<br>
     * <br>
     * Custom event batchers can be defined by implementing {@link com.koch.ambeth.event.IEventBatcher} and linking them to
     * {@link com.koch.ambeth.event.IEventBatcherExtendable}
     *
     * @see #enableEventQueue()
     */
    void flushEventQueue();
}
