package com.koch.ambeth.cache.datachange;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ISecondLevelCacheManager;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.event.RefreshEntitiesOfType;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.LockState;
import com.koch.ambeth.util.collections.ArrayList;

public class RefreshEntitiesEventListener implements IEventListener {
	@Autowired
	protected ISecondLevelCacheManager secondLevelCacheManager;

	@Autowired
	protected IRootCache committedRootCache;

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
		if (!(eventObject instanceof RefreshEntitiesOfType)) {
			return;
		}
		RefreshEntitiesOfType evnt = (RefreshEntitiesOfType) eventObject;

		final ClassExtendableContainer<Boolean> entityTypes = new ClassExtendableContainer<>(
				"toInvalidateFlag", "entityType");
		for (Class<?> entityType : evnt.getEntityTypes()) {
			entityTypes.register(Boolean.TRUE, entityType);
		}

		ICache currentCache = committedRootCache.getCurrentCache();
		ArrayList<IRootCache> allSecondLevelCaches = new ArrayList<>(
				secondLevelCacheManager.selectAllCurrentSecondLevelCaches());
		if (currentCache == committedRootCache) {
			// important to update the committed root cache first: the reason is that the other 2nd level
			// caches may be accessed concurrently. So if we invalidate them first and they get their
			// refresh by concurrently activity they would again fetch stalled data from the still
			// untouched committedRootCache
			allSecondLevelCaches.add(0, committedRootCache);
		}
		for (IRootCache rootCache : allSecondLevelCaches) {
			try {
				Lock writeLock = rootCache.getWriteLock();
				LockState lockState = null;
				if (writeLock.isReadLockHeld()) {
					lockState = writeLock.releaseAllLocks();
				}
				writeLock.lock();
				try {
					final ArrayList<IObjRef> objRefsToRemove = new ArrayList<>();
					rootCache.getContent(new HandleContentDelegate() {
						@Override
						public void invoke(Class<?> entityType, byte idIndex, Object id, Object value) {
							Boolean toInvalidate = entityTypes.getExtension(entityType);
							if (Boolean.TRUE.equals(toInvalidate)) {
								objRefsToRemove.add(new ObjRef(entityType, idIndex, id, null));
							}
						}
					});
					rootCache.remove(objRefsToRemove);
				}
				finally {
					writeLock.unlock();
					if (lockState != null) {
						writeLock.reacquireLocks(lockState);
					}
				}
			}
			catch (RuntimeException e) {
				// cache might already be disposed by a foreign thread
			}
		}
	}
}
