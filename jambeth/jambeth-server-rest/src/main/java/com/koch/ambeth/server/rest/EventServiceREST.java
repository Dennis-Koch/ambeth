package com.koch.ambeth.server.rest;

import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.service.IEventService;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.link.ILinkContainer;
import com.koch.ambeth.service.rest.Constants;
import com.koch.ambeth.util.collections.AbstractListElem;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.GenericFastList;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.StreamingOutput;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/EventService")
@Consumes({ Constants.AMBETH_MEDIA_TYPE })
@Produces({ Constants.AMBETH_MEDIA_TYPE })
public class EventServiceREST extends AbstractServiceREST implements IEventListener {
    private final GenericFastList<Entry> fastList = new GenericFastList<>(Entry.class);
    // it is mandatory that we have at least 2 threads: one is fixed-bound to the timeout-dispatcher
    // runnable registered in the constructor. at least one additional thread is needed to process
    // the DCE based runnables
    ExecutorService executor = Executors.newFixedThreadPool(2);
    private volatile boolean disposed;
    private long sleepInterval = 5000;
    private ILinkContainer link;

    public EventServiceREST() {
        executor.execute(() -> {
            while (!disposed) {
                try {
                    Thread.sleep(sleepInterval);

                    var requestedWithTimeout = resolvePendingRequests(true);
                    if (requestedWithTimeout.size() > 0) {
                        executor.execute(new NotifyRequestsRunnable(requestedWithTimeout));
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted(); // clear flag
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
                this.link = beanContext.link(this).to(IEventListenerExtendable.class).with(Object.class).finishLink();
            }
        } finally {
            writeLock.unlock();
        }
    }

    protected void dropPendingRequest(Entry runnable) {
        synchronized (fastList) {
            fastList.remove(runnable);
        }
    }

    protected List<Entry> resolvePendingRequests(boolean timedOutOnly) {
        synchronized (fastList) {
            if (fastList.size() == 0) {
                return List.of();
            }
            var clients = new ArrayList<Entry>(timedOutOnly ? 10 : fastList.size());
            var currTime = System.currentTimeMillis();
            var pointer = fastList.getFirstElem();
            while (pointer != null) {
                var next = pointer.getNext();
                if (!timedOutOnly || pointer.waitUntil <= currTime) {
                    clients.add(pointer);
                }
                pointer = next;
            }
            return clients;
        }
    }

    @Override
    public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
        var pendingRequests = resolvePendingRequests(false);
        if (pendingRequests.isEmpty()) {
            return;
        }
        executor.execute(new NotifyRequestsRunnable(pendingRequests));
    }

    @POST
    @Path("pollEvents")
    public StreamingOutput pollEvents(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, is, args -> getEventService().pollEvents(((Long) args[0]).longValue(), ((Long) args[1]).longValue(), ((Long) args[2]).longValue()));
    }

    @POST
    @Path("pollEventsAsync")
    public void pollEventsAsync(InputStream is, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        var rollback = preServiceCall(request, response);
        try {
            var args = getArguments(is, request);
            var serverSession = ((Long) args[0]).longValue();
            var eventSequenceSince = ((Long) args[1]).longValue();
            var requestedMaximumWaitTime = ((Long) args[2]).longValue();
            var waitUntil = System.currentTimeMillis() + requestedMaximumWaitTime;
            var asyncContext = request.startAsync(request, response);
            var entry = new Entry(asyncContext, waitUntil, createAsyncEventRequest(asyncContext, serverSession, eventSequenceSince, waitUntil));
            synchronized (fastList) {
                fastList.pushFirst(entry);
            }
        } finally {
            rollback.rollback();
        }
    }

    protected AsyncEventRequest createAsyncEventRequest(AsyncContext asyncContext, long serverSession, long eventSequenceSince, long waitUntil) {
        return forceDeliveryIfEmpty -> {
            var request = (HttpServletRequest) asyncContext.getRequest();
            var response = (HttpServletResponse) asyncContext.getResponse();
            var doComplete = forceDeliveryIfEmpty;
            var rollback = preServiceCall(request, response);
            try {
                var result = getEventService().pollEvents(serverSession, eventSequenceSince, 0);

                if (!forceDeliveryIfEmpty && result.isEmpty()) {
                    // do nothing with this async completion for the current caller. the request still waits till it
                    // a) times out, gets force executed and (presumably) returns an empty event response
                    // b) before timeout a change happens that leads to a non-empty event response for this caller
                    return false;
                }
                doComplete = true;
                var output = createResult(result, request, response);

                var sos = response.getOutputStream();
                output.write(new FlushSuppressingOutputStream(sos));
                sos.flush();
                sos.close();
            } catch (Throwable e) {
                doComplete = true;
                try {
                    var output = createExceptionResult(e, request, response);

                    var sos = response.getOutputStream();
                    output.write(new FlushSuppressingOutputStream(sos));
                    sos.flush();
                    sos.close();
                } catch (Throwable ex) {
                    getLog().error(ex);
                }
            } finally {
                rollback.rollback();
                if (doComplete) {
                    asyncContext.complete();
                }
            }
            return true;
        };
    }

    @GET
    @Path("getCurrentEventSequence")
    public StreamingOutput getCurrentEventSequence(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, () -> getEventService().getCurrentEventSequence());
    }

    @GET
    @Path("getCurrentServerSession")
    public StreamingOutput getCurrentServerSession(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        return defaultStreamingRequest(request, response, () -> getEventService().getCurrentServerSession());
    }

    protected void markResponseOngoingFalse(Entry pendingRequest) {
        synchronized (pendingRequest.getMutex()) {
            pendingRequest.setResponseOngoing(false);
            pendingRequest.notifyAll();
        }
    }

    protected boolean markResponseOngoingTrue(Entry pendingRequest) {
        synchronized (pendingRequest.getMutex()) {
            while (true) {
                var completed = pendingRequest.isCompleted();
                if (completed) {
                    return false;
                }
                if (pendingRequest.isResponseOngoing()) {
                    try {
                        pendingRequest.wait();
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        continue;
                    }
                }
                pendingRequest.setResponseOngoing(true);
                return true;
            }
        }
    }

    public interface AsyncEventRequest {
        boolean resolveAndDeliverResponse(boolean forceDeliveryIfEmpty);
    }

    @RequiredArgsConstructor
    private final class NotifyRequestsRunnable implements Runnable {
        final List<Entry> pendingRequests;

        @Override
        public void run() {
            try {
                var pendingRequests = this.pendingRequests;
                for (int a = 0, size = pendingRequests.size(); a < size; a++) {
                    var runnable = pendingRequests.get(a);
                    try {
                        if (!markResponseOngoingTrue(runnable)) {
                            continue;
                        }
                        var forceDeliveryIfEmpty = System.currentTimeMillis() <= runnable.waitUntil;
                        if (runnable.runnable.resolveAndDeliverResponse(forceDeliveryIfEmpty)) {
                            synchronized (runnable) {
                                runnable.setCompleted(true);
                            }
                            dropPendingRequest(runnable);
                        }
                    } finally {
                        markResponseOngoingFalse(runnable);
                    }
                }
            } catch (Throwable e) {
                getLog().error(e);
            }
        }
    }

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

    public static class Entry extends AbstractListElem<Entry> {
        AsyncContext asyncContext;

        long waitUntil;

        AsyncEventRequest runnable;

        @Getter
        @Setter
        volatile boolean completed;

        @Getter
        @Setter
        volatile boolean responseOngoing;

        public Entry(AsyncContext asyncContext, long waitUntil, AsyncEventRequest runnable) {
            this.asyncContext = asyncContext;
            this.waitUntil = waitUntil;
            this.runnable = runnable;
        }

        public Object getMutex() {
            return this;
        }
    }
}
