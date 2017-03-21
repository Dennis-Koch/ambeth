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

/**
 * Implemented by an implementation of {@link IEventListener} or {@link IEventTargetEventListener}
 * to additional mark a batch processing capability for the listener itself. Conceptionally a
 * listener is notified event-by-event. But if there are many events dispatched in a batch by using
 * the {@link IEventQueue#enableEventQueue()} construct it may be of interest for the listeners to
 * know the scope of this batch.<br>
 * <br>
 *
 * Do not call these methods here explicitly. The Ambeth {@link EventListenerRegistry} deals with
 * calling them whenever a batched dispatching happens and the current listener is part of the
 * listener list for at least one event of that batch. If that is the case the
 * {@link EventListenerRegistry} calls also {@link #flushBatchedEventDispatching()} after the
 * complete batch has been dispatched.
 */
public interface IBatchedEventListener extends IEventListenerMarker {
	/**
	 * Notifies the implementing listener that the following events being dispatched in the same
	 * (current) thread have been dispatched in a batch.
	 *
	 * @see IEventQueue#enableEventQueue()
	 * @see IBatchedEventListener
	 */
	void enableBatchedEventDispatching();

	/**
	 * Notifies the implementing listener that the batch dispatching of events in the same (current)
	 * thread has been completed.
	 *
	 * @see IEventQueue#flushEventQueue()
	 * @see IBatchedEventListener
	 */
	void flushBatchedEventDispatching();
}
