package com.koch.ambeth.cache.datachange.revert;

import java.util.Collection;

public class CollectionBackup implements IBackup {
	public static final IBackup EMPTY_COLLECTION_BACKUP = new CollectionBackup(new Object[0]);

	public static IBackup create(Object[] arrayClone) {
		if (arrayClone == null || arrayClone.length == 0) {
			return EMPTY_COLLECTION_BACKUP;
		}
		return new CollectionBackup(arrayClone);
	}

	protected final Object[] arrayClone;

	private CollectionBackup(Object[] arrayClone) {
		this.arrayClone = arrayClone;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void restore(Object target) {
		Collection targetList = (Collection) target;
		targetList.clear();
		for (int index = 0, size = arrayClone.length; index < size; index++) {
			Object itemClone = arrayClone[index];
			targetList.add(itemClone);
		}
	}
}
