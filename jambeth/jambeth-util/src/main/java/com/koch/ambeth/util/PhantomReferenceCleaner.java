package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-util
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

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.collections.IdentityHashSet;

/**
 * This is best used to allow calling cleanup code similar to the possibility to implement
 * Object.finalize(). In contrast to Object.finalize() the PhantomReference behavior does less
 * overhead to the GC algorithms at Runtime. Because of this Object.finalize() is considered a
 * deprecated and discouraged design pattern.
 *
 * Usage:
 *
 * <code>
 * PhantomReferenceCleaner&gt;Connection, ConnectionPhantomRef&lt; phantomReferenceCleaner = new PhantomReferenceCleaner&gt;Connection, ConnectionPhantomRef&lt;()
 * 	{
 *
 * @Override protected void doCleanup(ConnectionPhantomRef phantom) { phantom.closeConnection(); } }; </code>
 *
 *           <code>
 * public class ConnectionPhantomRef extends PhantomReference&gt;Connection&lt;
 * {
 * 	private Connection targetConnection;
 *
 * 	public ConnectionPhantomRef(Connection referent, ReferenceQueue&gt;Connection&lt; q, Connection targetConnection)
 * 	{
 * 		super(referent, q);
 * 		if (referent == targetConnection)
 * 		{
 * 			throw new IllegalArgumentException("Can not refer to target connection itself");
 * 		}
 * 		this.targetConnection = targetConnection;
 * 	}
 *
 * 	public void closeConnection()
 * 	{
 * 		JdbcUtil.close(targetConnection);
 * 		targetConnection = null;
 * 	}
 * }
 * </code>
 *
 *           <code>
 * phantomReferenceCleaner.queue(new ConnectionPhantomRef(proxiedConnection, phantomReferenceCleaner.getReferenceQueue(), connection));
 * </code>
 */
public abstract class PhantomReferenceCleaner<DelegateType, PhantomType extends PhantomReference<DelegateType>> {
	protected final ReferenceQueue<DelegateType> referenceQueue = new ReferenceQueue<>();

	protected final IdentityHashSet<PhantomType> phantomRefInUseSet =
			new IdentityHashSet<>();

	protected final Lock writeLock = new ReentrantLock();

	public ReferenceQueue<DelegateType> getReferenceQueue() {
		return referenceQueue;
	}

	public void queue(PhantomType pr) {
		writeLock.lock();
		try {
			phantomRefInUseSet.add(pr);
		}
		finally {
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	public void checkForCleanup() {
		Reference<? extends DelegateType> phantomReferenceToCleanup;
		while ((phantomReferenceToCleanup = referenceQueue.poll()) != null) {
			writeLock.lock();
			try {
				phantomRefInUseSet.remove(phantomReferenceToCleanup);
			}
			finally {
				writeLock.unlock();
			}
			doCleanup((PhantomType) phantomReferenceToCleanup);
		}
	}

	protected abstract void doCleanup(PhantomType phantom);
}
