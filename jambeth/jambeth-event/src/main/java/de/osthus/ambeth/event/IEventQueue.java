package de.osthus.ambeth.event;

public interface IEventQueue
{
	/**
	 * Used together with {@link #flushEventQueue()} to prepare bulk-dispatching operations. After invoking this method all dispatching operations to
	 * {@link IEventDispatcher#dispatchEvent(Object)} and its overloads are queued up and wait for a {@link #flushEventQueue()}.<br>
	 * </br> This method stacks: Multiple "enable" calls need the same amount of "flush" calls till a real flush is done and the right {@link IEventListener}s
	 * are notified. See also {@link #flushEventQueue()} for more details about whats happening during the flush. It is recommended to use it consistently in a
	 * try-finally statement:
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
	 * 
	 */
	void enableEventQueue();

	/**
	 * Used together with {@link #enableEventQueue()} to prepare bulk-dispatching operations. If the internal stack count (together with
	 * {@link #enableEventQueue()}) reaches zero the queued events are "batched" together: This means some optional compact algorithm may process the events -
	 * e.g. to reduce potential redundancy of events which somehow "outdate" previous events. After the batch sequence the remaining (or newly created,
	 * compacted) events get dispatched to the registered {@link IEventListener}s.<br/>
	 * <br/>
	 * Custom event batchers can be defined by implementing {@link IEventBatcher} and linking them to {@link IEventBatcherExtendable}
	 * 
	 * @see #enableEventQueue()
	 */
	void flushEventQueue();

	/**
	 * Propagates that the given eventTarget is now "paused" for the eventqueue-dispatching engine. However the method blocks if the eventqueue-dispatching
	 * engine is already processing an event correlating to the given eventTarget by using the
	 * {@link IEventDispatcher#waitEventToResume(Object, long, de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate, de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate)}
	 * method.<br/>
	 * <br/>
	 * If the given eventTarget is a proxy instance of something then there might be a need for an instance of {@link IEventTargetExtractor} which is to be
	 * registered to {@link IEventTargetExtractorExtendable}. The eventdispatching-engine considers this extractor to resolve the real "target eventHandle"
	 * needed for identity equality checks when resolving the correct "paused" states.
	 * 
	 * @param eventTarget
	 */
	void pause(Object eventTarget);

	/**
	 * Propagates that the given eventTarget is now again "free" for the eventqueue-dispatching engine.<br/>
	 * <br/>
	 * If the given eventTarget is a proxy instance of something then there might be a need for an instance of {@link IEventTargetExtractor} which is to be
	 * registered to {@link IEventTargetExtractorExtendable}. The eventdispatching-engine considers this extractor to resolve the real "target eventHandle"
	 * needed for identity equality checks when resolving the correct "paused" states.
	 * 
	 * @param eventTarget
	 */
	void resume(Object eventTarget);

	/**
	 * Evaluates whether the current thread is flagged as dispatching a batch of events
	 * 
	 * @return true if and only if the current thread is flagged as dispatching a batch of events
	 */
	boolean isDispatchingBatchedEvents();
}
