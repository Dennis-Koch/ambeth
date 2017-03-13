package com.koch.ambeth.util.threading;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public interface IFastThreadPool extends Executor
{

	void queueAction(final HandlerRunnable<?, ?> handlerRunnable);

	<O> void queueAction(final O object, final HandlerRunnable<O, ?> handlerRunnable, final CountDownLatch latch);

	<O> void queueActions(final List<O> objects, final HandlerRunnable<O, ?> handlerRunnable, final CountDownLatch latch);

	<O> void queueActionsWait(final List<O> objects, final HandlerRunnable<O, ?> handlerRunnable);

	<O, C> void queueActionsWait(final List<O> objects, final C context, final HandlerRunnable<O, C> handlerRunnable);

	<O, C> void queueActions(final List<O> objects, final C context, final HandlerRunnable<O, C> handlerRunnable, final CountDownLatch latch);

	<O, C> void queueAction(final O object, final C context, final HandlerRunnable<O, C> handlerRunnable, final CountDownLatch latch);
}