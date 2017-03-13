package com.koch.ambeth.event;

import java.util.List;

import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.event.model.IEventItem;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.IOfflineListener;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamHolder;

public class EventPoller implements IEventPoller, IOfflineListener, IStartingBean, IDisposable {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IEventService eventService;

	protected long pollSleepInterval;

	protected long maxWaitInterval;

	protected boolean startPaused;

	protected Object writeLock = new Object();

	protected volatile boolean stopRequested = false;

	protected volatile boolean pauseRequested = false;

	protected int iterationId = 1;

	@Override
	public void afterStarted() throws Throwable {
		if (startPaused) {
			pausePolling();
		}
		startPolling();
	}

	@Override
	public void dispose() {
		stopPolling();
	}

	@Property(name = EventConfigurationConstants.PollingSleepInterval, defaultValue = "500")
	public void setPollSleepInterval(long pollSleepInterval) {
		this.pollSleepInterval = pollSleepInterval;
	}

	@Property(name = EventConfigurationConstants.MaxWaitInterval, defaultValue = "30000")
	public void setMaxWaitInterval(long maxWaitInterval) {
		this.maxWaitInterval = maxWaitInterval;
	}

	@Property(name = EventConfigurationConstants.StartPausedActive, defaultValue = "false")
	public void setStartPaused(boolean startPaused) {
		this.startPaused = startPaused;
	}

	public void stopPolling() {
		synchronized (writeLock) {
			stopRequested = true;
			pauseRequested = false;
			iterationId++;
		}
	}

	public void startPolling() {
		final int stackIterationId = iterationId;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					long currentServerSession = eventService.getCurrentServerSession();
					long currentEventSequence = eventService.getCurrentEventSequence();

					IParamHolder<Boolean> errorOccured = new ParamHolder<Boolean>();
					while (!stopRequested && stackIterationId == iterationId) {
						synchronized (writeLock) {
							while (pauseRequested && !stopRequested) {
								if (stopRequested) {
									break;
								}
							}
						}
						currentEventSequence =
								tryPolling(currentServerSession, currentEventSequence, errorOccured);
						if (errorOccured.getValue().booleanValue()) {
							Thread.sleep(Math.max(5000, pollSleepInterval));
						}
						else {
							Thread.sleep(pollSleepInterval);
						}
					}
				}
				catch (Exception e) {
					if (log.isErrorEnabled()) {
						log.error(e);
					}
				}
			}
		});
		thread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		thread.setName("Event Polling");
		thread.setDaemon(true);
		thread.start();
	}

	protected long tryPolling(long currentServerSession, long currentEventSequence,
			IParamHolder<Boolean> errorOccured) {
		errorOccured.setValue(Boolean.TRUE);
		List<IEventItem> events = null;
		try {
			events = eventService.pollEvents(currentServerSession, currentEventSequence, maxWaitInterval);
			errorOccured.setValue(Boolean.FALSE);
		}
		catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error(e);
			}
		}
		if (events == null || events.size() == 0) {
			return currentEventSequence;
		}
		long timeBeforeDispatch = System.currentTimeMillis();
		eventDispatcher.enableEventQueue();
		try {
			for (int a = 0, size = events.size(); a < size; a++) {
				IEventItem eventObject = events.get(a);
				eventDispatcher.dispatchEvent(eventObject.getEventObject(), eventObject.getDispatchTime(),
						eventObject.getSequenceNumber());
				currentEventSequence = eventObject.getSequenceNumber();
			}
		}
		finally {
			eventDispatcher.flushEventQueue();
		}
		if (log.isInfoEnabled()) {
			long timeAfterDispatch = System.currentTimeMillis();
			log.info("Dispatching" + events.size() + "events took "
					+ (timeAfterDispatch - timeBeforeDispatch) + " ms.");
		}

		return currentEventSequence;
	}

	@Override
	public void pausePolling() {
		synchronized (writeLock) {
			if (pauseRequested) {
				return;
			}
			if (log.isInfoEnabled()) {
				log.info("Polling activated, but paused for concurrency reasons till "
						+ IEventPoller.class.getSimpleName() + ".resumePolling() is called");
			}
			pauseRequested = true;
		}
	}

	@Override
	public void resumePolling() {
		synchronized (writeLock) {
			if (!pauseRequested) {
				return;
			}
			if (log.isInfoEnabled()) {
				log.info("Polling resumed");
			}
			pauseRequested = false;
		}
	}

	@Override
	public void beginOnline() {
		stopPolling();
	}

	@Override
	public void handleOnline() {
		// Intended blank
	}

	@Override
	public void endOnline() {
		startPolling();
	}

	@Override
	public void beginOffline() {
		stopPolling();
	}

	@Override
	public void handleOffline() {
		// Intended blank
	}

	@Override
	public void endOffline() {
		startPolling();
	}
}
