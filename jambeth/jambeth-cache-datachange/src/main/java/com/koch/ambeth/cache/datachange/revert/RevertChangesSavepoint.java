package com.koch.ambeth.cache.datachange.revert;

import java.util.Map.Entry;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.util.collections.IMap;

public class RevertChangesSavepoint implements IRevertChangesSavepoint {
	public static final String P_CHANGES = "Changes";

	@Autowired
	protected ICacheModification cacheModification;

	@Property
	protected IMap<Object, IBackup> changes;

	protected final long savepointTime = System.currentTimeMillis();

	@Override
	public void dispose() {
		changes = null;
	}

	@Override
	public Object[]

			getSavedBusinessObjects() {
		return changes.keyList().toArray(Object.class);
	}

	@Override
	public void revertChanges() {
		if (changes == null) {
			throw new IllegalStateException("This object has already been disposed");
		}
		boolean oldCacheModificationValue = cacheModification.isActive();
		cacheModification.setActive(true);
		try {
			for (Entry<Object, IBackup> entry : changes) {
				entry.getValue().restore(entry.getKey());
			}
		} finally {
			cacheModification.setActive(oldCacheModificationValue);
		}
	}
}
