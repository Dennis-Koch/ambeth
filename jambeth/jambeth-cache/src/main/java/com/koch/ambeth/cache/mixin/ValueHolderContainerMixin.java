package com.koch.ambeth.cache.mixin;

/*-
 * #%L
 * jambeth-cache
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

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.cache.transfer.ObjRelation;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.NoOpStateRollback;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class ValueHolderContainerMixin implements IDisposableBean, IAsyncLazyLoadController {
	@Autowired
	protected ICacheHelper cacheHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired(optional = true)
	protected ILightweightTransaction transaction;

	protected volatile boolean disposed;

	protected final HashSet<DirectValueHolderRef> vhRefToPendingEventHandlersMap =
			new HashSet<>();

	protected final Lock writeLock = new ReentrantLock();

	protected final Condition cond = writeLock.newCondition(),
			haveDataCond = writeLock.newCondition(), sleepingCond = writeLock.newCondition();

	protected final ThreadLocal<Boolean> asynchronousResultAllowedTL = new ThreadLocal<>();

	protected long queueInterval = 100;

	protected Thread thread;

	protected volatile boolean sleeping = true;

	@Override
	public void destroy() throws Throwable {
		disposed = true;
		writeLock.lock();
		try {
			thread = null;
			cond.signal();
			haveDataCond.signal();
		}
		finally {
			writeLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.koch.ambeth.cache.mixin.IAsyncLazyLoadController#pushAsynchronousResultAllowed(com.koch.
	 * ambeth.util.state.IStateRollback)
	 */
	@Override
	public IStateRollback pushAsynchronousResultAllowed(IStateRollback... rollbacks) {
		final Boolean old = asynchronousResultAllowedTL.get();
		if (Boolean.TRUE.equals(old)) {
			return NoOpStateRollback.createNoOpRollback(rollbacks);
		}
		asynchronousResultAllowedTL.set(Boolean.TRUE);
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				asynchronousResultAllowedTL.set(old);
			}
		};
	}

	protected void loadAllPendingValueHolders(DirectValueHolderRef[] vhRefs) {
		prefetchHelper.prefetch(vhRefs);
	}

	public IObjRelation getSelf(Object entity, String memberName) {
		IList<IObjRef> allObjRefs = objRefHelper.entityToAllObjRefs(entity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), memberName);
	}

	public IObjRelation getSelf(IObjRefContainer entity, int relationIndex) {
		String memberName = entity.get__EntityMetaData().getRelationMembers()[relationIndex].getName();
		IList<IObjRef> allObjRefs = objRefHelper.entityToAllObjRefs(entity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), memberName);
	}

	public Object getValue(IObjRefContainer entity, RelationMember[] relationMembers,
			int relationIndex, ICacheIntern targetCache, IObjRef[] objRefs) {
		return getValue(entity, relationIndex, relationMembers[relationIndex], targetCache, objRefs,
				CacheDirective.none());
	}

	public Object getValue(IObjRefContainer entity, int relationIndex, RelationMember relationMember,
			final ICacheIntern targetCache, IObjRef[] objRefs, final Set<CacheDirective> cacheDirective) {
		if (targetCache == null) {
			// This happens if an entity gets newly created and immediately called for relations (e.g.
			// collections to add sth)
			return cacheHelper.createInstanceOfTargetExpectedType(relationMember.getRealType(),
					relationMember.getElementType());
		}
		IGuiThreadHelper guiThreadHelper = this.guiThreadHelper;
		boolean isInGuiThread = guiThreadHelper.isInGuiThread();
		ValueHolderState state = entity.get__State(relationIndex);
		boolean initPending = ValueHolderState.PENDING == state;
		boolean asynchronousResultAllowed = Boolean.TRUE.equals(asynchronousResultAllowedTL.get());
		if (isInGuiThread && initPending) {
			// Content is not really loaded, but instance is available to use (SOLELY for DataBinding in
			// GUI Thread)
			Object value = ((IValueHolderContainer) entity).get__ValueDirect(relationIndex);
			if (value != null) {
				return value;
			}
		}
		if (!asynchronousResultAllowed) {
			IList<Object> results;
			if (objRefs == null) {
				final IObjRelation self = getSelf(entity, relationMember.getName());

				if (transaction != null) {
					results = transaction
							.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<IList<Object>>() {
								@Override
								public IList<Object> invoke() throws Exception {
									IList<IObjRelationResult> objRelResults = targetCache
											.getObjRelations(Arrays.asList(self), targetCache, cacheDirective);
									if (objRelResults.isEmpty()) {
										return EmptyList.getInstance();
									}
									else {
										IObjRelationResult objRelResult = objRelResults.get(0);
										return targetCache.getObjects(
												new ArrayList<IObjRef>(objRelResult.getRelations()), targetCache,
												cacheDirective);
									}
								}
							});
				}
				else {
					IList<IObjRelationResult> objRelResults = targetCache.getObjRelations(Arrays.asList(self),
							targetCache, cacheDirective);
					if (objRelResults.isEmpty()) {
						results = EmptyList.getInstance();
					}
					else {
						IObjRelationResult objRelResult = objRelResults.get(0);
						results = targetCache.getObjects(new ArrayList<IObjRef>(objRelResult.getRelations()),
								targetCache, cacheDirective);
					}
				}
			}
			else {
				results = targetCache.getObjects(new ArrayList<IObjRef>(objRefs), targetCache,
						cacheDirective);
			}
			return cacheHelper.convertResultListToExpectedType(results, relationMember.getRealType(),
					relationMember.getElementType());
		}
		writeLock.lock();
		try {
			if (disposed) {
				return null;
			}
			((IValueHolderContainer) entity).set__InitPending(relationIndex);
			vhRefToPendingEventHandlersMap.add(new DirectValueHolderRef(entity, relationMember));
			ensureThread();
			haveDataCond.signal();
		}
		finally {
			writeLock.unlock();
		}
		return cacheHelper.createInstanceOfTargetExpectedType(relationMember.getRealType(),
				relationMember.getElementType());
	}

	private void ensureThread() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		writeLock.lock();
		try {
			sleeping = false;
		}
		finally {
			writeLock.unlock();
		}
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!disposed) {
					DirectValueHolderRef[] vhRefs;
					writeLock.lock();
					try {
						sleeping = false;
						if (vhRefToPendingEventHandlersMap.size() == 0) {
							sleeping = true;
							sleepingCond.signalAll();
							haveDataCond.await();
							continue;
						}
						cond.await(queueInterval, TimeUnit.MILLISECONDS);
						vhRefs =
								vhRefToPendingEventHandlersMap.toArray(DirectValueHolderRef.class);
						vhRefToPendingEventHandlersMap.clear();
						if (disposed) {
							return;
						}
					}
					catch (InterruptedException e) {
						Thread.interrupted(); // clear flag
						continue;
					}
					finally {
						writeLock.unlock();
					}
					loadAllPendingValueHolders(vhRefs);
				}
			}
		});
		thread.setName(getClass().getName());
		thread.setDaemon(true);
		thread.start();
	}

	public Object getValue(IValueHolderContainer vhc, int relationIndex) {
		return getValue(vhc, relationIndex, CacheDirective.none());
	}

	public Object getValue(IValueHolderContainer vhc, int relationIndex,
			Set<CacheDirective> cacheDirective) {
		IEntityMetaData metaData = vhc.get__EntityMetaData();
		RelationMember relationMember = metaData.getRelationMembers()[relationIndex];
		if (ValueHolderState.INIT == vhc.get__State(relationIndex)) {
			return relationMember.getValue(vhc);
		}
		IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
		return getValue(vhc, relationIndex, relationMember, vhc.get__TargetCache(), objRefs,
				cacheDirective);
	}

	@Override
	public void awaitAsyncWorkload() throws InterruptedException {
		writeLock.lock();
		try {
			while (!sleeping) {
				sleepingCond.await();
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean awaitAsyncWorkload(long time, TimeUnit unit) throws InterruptedException {
		long waitTill = System.currentTimeMillis() + unit.toMillis(time);
		writeLock.lock();
		try {
			while (!sleeping) {
				long maxWait = waitTill - System.currentTimeMillis();
				if (maxWait <= 0) {
					return false;
				}
				if (sleepingCond.await(maxWait, TimeUnit.MILLISECONDS)) {
					return true;
				}
			}
			return false;
		}
		finally {
			writeLock.unlock();
		}
	}
}
