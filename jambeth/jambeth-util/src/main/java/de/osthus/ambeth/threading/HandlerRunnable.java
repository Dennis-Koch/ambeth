package de.osthus.ambeth.threading;

import java.util.concurrent.CountDownLatch;

public abstract class HandlerRunnable<O, C>
{
	public abstract void handle(final O object, final C context, final CountDownLatch latch);
}
