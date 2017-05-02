package com.koch.ambeth.cache.datachange.revert;

import java.util.List;

public class ListBackup implements IBackup {
	public static IBackup create(Object[] arrayClone) {
		if (arrayClone == null || arrayClone.length == 0) {
			return CollectionBackup.EMPTY_COLLECTION_BACKUP;
		}
		return new ListBackup(arrayClone);
	}

	protected final Object[] arrayClone;

	private ListBackup(Object[] arrayClone) {
		this.arrayClone = arrayClone;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void restore(Object target) {
		List targetList = (List) target;
		while (targetList.size() > arrayClone.length) {
			targetList.remove(targetList.size() - 1);
		}
		for (int index = 0, size = arrayClone.length; index < size; index++) {
			Object itemClone = arrayClone[index];
			if (targetList.size() <= index) {
				targetList.add(itemClone);
				continue;
			}
			if (targetList.get(index) != itemClone) {
				// Set only if the reference is really different
				// This is due to the fact that some list implementations fire
				// PropertyChangeEvents even if the index value references did not change
				targetList.set(index, itemClone);
			}
		}
	}
}
