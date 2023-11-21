package com.koch.ambeth.server.rest;

import java.io.IOException;

/*-
 * #%L
 * jambeth-server-rest
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.StreamingOutput;

import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.model.IEventItem;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.link.ILinkContainer;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.collections.AbstractListElem;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.GenericFastList;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

@Path("/EventService")
@Consumes({Constants.AMBETH_MEDIA_TYPE})
@Produces({Constants.AMBETH_MEDIA_TYPE})
public class EventServiceREST extends AbstractServiceREST implements IEventListener {
	private static class FlushSuppressingOutputStream extends OutputStream {
		private final OutputStream os;

		public FlushSuppressingOutputStream(OutputStream os) {
			this.os = os;
		}

		@Override
		public void write(int b) throws IOException {
			os.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			os.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			os.write(b, off, len);
		}
	}

	private final class NotifyRequestsRunnable implements Runnable {
		private final List<IBackgroundWorkerDelegate> clients;

		private NotifyRequestsRunnable(List<IBackgroundWorkerDelegate> clients) {
			this.clients = clients;
		}

		@Override
		public void run() {
			for (int a = 0, size = clients.size(); a < size; a++) {
				var runnable = clients.get(a);
				try {
					runnable.invoke();
				}
				catch (Throwable e) {
					getLog().error(e);
				}
			}
		}
	}

	public static class Entry extends AbstractListElem<Entry> {
		AsyncContext asyncContext;

		long waitUntil;

		IBackgroundWorkerDelegate runnable;

		public Entry() {
			// intended blank
		}

		public Entry(AsyncContext asyncContext, long waitUntil, IBackgroundWorkerDelegate runnable) {
			this.asyncContext = asyncContext;
			this.waitUntil = waitUntil;
			this.runnable = runnable;
		}
	}

	private final GenericFastList<Entry> fastList = new GenericFastList<>(Entry.class);

	// it is mandatory that we have at least 2 threads: one is fixed-bound to the timeout-dispatcher
	// runnable registered in the constructor. at least one additional thread is needed to process
	// the DCE based runnables
	ExecutorService executor = Executors.newFixedThreadPool(2);

	private volatile boolean disposed;

	private long sleepInterval = 5000;

	private ILinkContainer link;

	public EventServiceREST() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (!disposed) {
					try {
						Thread.sleep(sleepInterval);

						List<IBackgroundWorkerDelegate> requestedWithTimeout =
								consumePendingRequests(false);
						if (requestedWithTimeout.size() > 0) {
							executor.execute(new NotifyRequestsRunnable(requestedWithTimeout));
						}
					}
					catch (InterruptedException e) {
						Thread.interrupted(); // clear flag
					}
				}
			}
		});
	}

	@Override
	protected void finalize() throws Throwable {
		disposed = true;
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
		super.finalize();
	}

	protected IEventService getEventService() {
		return getService(IEventService.class);
	}

	@Override
	public void setBeanContext(IServiceContext beanContext) {
		writeLock.lock();
		try {
			var link = this.link;
			if (link != null) {
				this.link = null;
				link.unlink();
			}
			super.setBeanContext(beanContext);
			if (beanContext != null) {
				this.link = beanContext.link(this).to(IEventListenerExtendable.class).with(Object.class)
						.finishLink();
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	private List<IBackgroundWorkerDelegate> consumePendingRequests(boolean consumeAll) {
		List<IBackgroundWorkerDelegate> clients;
		synchronized (fastList) {
			if (fastList.size() == 0) {
				return EmptyList.getInstance();
			}
			clients = new ArrayList<>(consumeAll ? fastList.size() : 10);
			var currTime = System.currentTimeMillis();
			var pointer = fastList.getFirstElem();
			while (pointer != null) {
				var next = pointer.getNext();
				if (consumeAll || pointer.waitUntil <= currTime) {
					fastList.remove(pointer);
					clients.add(pointer.runnable);
				}
				pointer = next;
			}
		}
		return clients;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
		var clients = consumePendingRequests(true);
		if (clients.isEmpty()) {
			return;
		}
		executor.execute(new NotifyRequestsRunnable(clients));
	}

	@POST
	@Path("pollEvents")
	public StreamingOutput pollEvents(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		var rollback = preServiceCall(request, response);
		try {
			var args = getArguments(is, request);
			var result = getEventService().pollEvents(((Long) args[0]).longValue(),
					((Long) args[1]).longValue(), ((Long) args[2]).longValue());
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@POST
	@Path("pollEventsAsync")
	public void pollEventsAsync(InputStream is, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		var rollback = preServiceCall(request, response);
		try {
			var args = getArguments(is, request);
			var serverSession = ((Long) args[0]).longValue();
			var eventSequenceSince = ((Long) args[1]).longValue();
			var requestedMaximumWaitTime = ((Long) args[2]).longValue();
			var waitUntil = System.currentTimeMillis() + requestedMaximumWaitTime;
			var asyncContext = request.startAsync(request, response);
			synchronized (fastList) {
				fastList.pushFirst(
						new Entry(asyncContext, waitUntil, new IBackgroundWorkerDelegate() {
							@Override
							public void invoke() throws Exception {
								var asyncRequest = (HttpServletRequest) asyncContext.getRequest();
								var asyncResponse =
										(HttpServletResponse) asyncContext.getResponse();
								var rollback2 = preServiceCall(asyncRequest, asyncResponse);
								try {
									var result = getEventService().pollEvents(serverSession,
											eventSequenceSince, waitUntil - System.currentTimeMillis());

									var output = createResult(result, asyncRequest, asyncResponse);

									var sos = asyncResponse.getOutputStream();
									output.write(new FlushSuppressingOutputStream(sos));
									sos.flush();
									sos.close();
								}
								catch (Throwable e) {
									try {
										var output = createExceptionResult(e, asyncRequest, asyncResponse);

										var sos = asyncResponse.getOutputStream();
										output.write(new FlushSuppressingOutputStream(sos));
										sos.flush();
										sos.close();
									}
									catch (Throwable ex) {
										ex.printStackTrace();
										getLog().error(ex);
									}
								}
								finally {
									rollback2.rollback();
									asyncContext.complete();
								}
							}
						}));
			}
		}
		finally {
			rollback.rollback();
		}
	}

	@GET
	@Path("getCurrentEventSequence")
	public StreamingOutput getCurrentEventSequence(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		var rollback = preServiceCall(request, response);
		try {
			var result = getEventService().getCurrentEventSequence();
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}

	@GET
	@Path("getCurrentServerSession")
	public StreamingOutput getCurrentServerSession(@Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		var rollback = preServiceCall(request, response);
		try {
			var result = getEventService().getCurrentServerSession();
			return createResult(result, request, response);
		}
		catch (Throwable e) {
			return createExceptionResult(e, request, response);
		}
		finally {
			rollback.rollback();
		}
	}
}
