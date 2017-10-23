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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.koch.ambeth.event.config.EventConfigurationConstants;
import com.koch.ambeth.event.events.EventSessionChanged;
import com.koch.ambeth.event.exceptions.EventPollException;
import com.koch.ambeth.event.model.IEventItem;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.IOfflineListener;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.collections.specialized.PropertyChangeSupport;

public class EventPoller extends PropertyChangeSupport
		implements IEventPoller, IOfflineListener, IStartingBean, IDisposableBean {
	@LogInstance
	private ILogger log;

	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IEventService eventService;

	@Property(name = EventConfigurationConstants.PollingSleepInterval, defaultValue = "100")
	protected long pollSleepInterval;

	@Property(name = EventConfigurationConstants.MaxWaitInterval, defaultValue = "30000")
	protected long maxWaitInterval;

	@Property(name = EventConfigurationConstants.StartPausedActive, defaultValue = "false")
	protected boolean startPaused;

	protected Object writeLock = new Object();

	protected volatile boolean stopRequested = false;

	protected volatile boolean pauseRequested = false;

	protected volatile int iterationId = 1;

	private volatile Thread thread;

	private volatile boolean connected;

	@Override
	public void afterStarted() throws Throwable {
		if (startPaused) {
			pausePolling();
		}
		startPolling();
	}

	@Override
	public void destroy() throws Throwable {
		stopPolling();
	}

	public void stopPolling() {
		synchronized (writeLock) {
			stopRequested = true;
			pauseRequested = false;
			iterationId++;
			writeLock.notifyAll();
			Thread thread = this.thread;
			if (thread != null) {
				thread.interrupt();
			}
		}
	}

	public void startPolling() {
		final int stackIterationId = iterationId;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (thread == Thread.currentThread()) {
					firePropertyChange(this, P_ACTIVE, false, true);
				}
				try {
					long currentServerSession = 0;
					long currentEventSequence = 0;

					AtomicBoolean errorOccured = new AtomicBoolean();
					loop: while (!stopRequested && stackIterationId == iterationId) {
						try {
							synchronized (writeLock) {
								while (pauseRequested) {
									if (stopRequested) {
										break loop;
									}
									writeLock.wait();
								}
							}
							if (currentServerSession == 0) {
								currentServerSession = eventService.getCurrentServerSession();
								if (thread == Thread.currentThread()) {
									setConnected(true);
								}
							}
							if (currentEventSequence == 0) {
								currentEventSequence = eventService.getCurrentEventSequence();
								setConnected(true);
							}
							currentEventSequence = tryPolling(currentServerSession, currentEventSequence,
									errorOccured);
							if (stopRequested) {
								break;
							}
							if (errorOccured.get()) {
								setConnected(false);
								long sleepTime = Math.max(5000, pollSleepInterval);
								if (log.isDebugEnabled()) {
									log.debug("Error occured. Sleeping for at least " + sleepTime + "ms");
								}
								Thread.sleep(Math.max(5000, pollSleepInterval));
							}
							else if (pollSleepInterval > 0) {
								if (log.isDebugEnabled()) {
									log.debug("Sleeping for at least " + pollSleepInterval + "ms");
								}
								Thread.sleep(pollSleepInterval);
							}
						}
						catch (InterruptedException ignored) {
							Thread.interrupted();
						}
					}
				}
				catch (Throwable e) {
					if (!stopRequested) {
						if (log.isErrorEnabled()) {
							log.error(e);
						}
					}
				}
				finally {
					if (thread == Thread.currentThread()) {
						setConnected(false);
						firePropertyChange(this, P_ACTIVE, true, false);
					}
				}
			}
		});
		thread.setContextClassLoader(classLoaderProvider.getClassLoader());
		thread.setName("Event Polling");
		thread.setDaemon(true);
		thread.start();
	}

	protected long tryPolling(long currentServerSession, long currentEventSequence,
			AtomicBoolean errorOccured) {
		errorOccured.set(true);
		List<IEventItem> events = null;
		try {
			events = eventService.pollEvents(currentServerSession, currentEventSequence, maxWaitInterval);
			errorOccured.set(false);
		}
		catch (Exception e) {
			if (pauseRequested || stopRequested) {
				return -1;
			}
			if (log.isErrorEnabled()) {
				log.error(e);
			}
			Throwable currEx = e;
			while (currEx != null) {
				if (currEx instanceof EventPollException) {
					long newServerSession = eventService.getCurrentServerSession();
					eventDispatcher.dispatchEvent(
							new EventSessionChanged(eventService, currentServerSession, newServerSession));
					break;
				}
				currEx = currEx.getCause();
			}
		}
		if (events == null || events.isEmpty()) {
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
			writeLock.notifyAll();
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

	@Override
	public boolean isActive() {
		Thread thread = this.thread;
		return thread != null && thread.isAlive();
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	protected void setConnected(boolean connected) {
		boolean oldConnected = this.connected;
		if (oldConnected == connected) {
			return;
		}
		this.connected = connected;
		firePropertyChange(this, P_CONNECTED, oldConnected, connected);
	}
}
