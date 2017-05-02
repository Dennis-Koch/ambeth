package com.koch.ambeth.cache.datachange.revert;

import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.SmartCopyMap;

public class ObjRefBackup implements IBackup {
	private static final SmartCopyMap<Integer, ObjRefBackup> relationIndexToEmptyBackupMap =
			new SmartCopyMap<>(0.5f);

	private static final SmartCopyMap<Integer, ObjRefBackup> relationIndexToNullBackupMap =
			new SmartCopyMap<>(0.5f);

	public static IBackup create(IObjRef[] objRefs, int relationIndex) {
		if (objRefs != null && objRefs.length > 0) {
			return new ObjRefBackup(objRefs, relationIndex);
		}
		SmartCopyMap<Integer, ObjRefBackup> map;
		if (objRefs == null) {
			map = relationIndexToNullBackupMap;
		}
		else {
			map = relationIndexToEmptyBackupMap;
		}
		ObjRefBackup backup = map.get(relationIndex);
		if (backup != null) {
			return backup;
		}
		backup = new ObjRefBackup(objRefs, relationIndex);
		if (!map.putIfNotExists(relationIndex, backup)) {
			// concurrent thread has been faster
			backup = map.get(relationIndex);
		}
		return backup;
	}

	protected final IObjRef[] objRefs;
	protected final int relationIndex;

	private ObjRefBackup(IObjRef[] objRefs, int relationIndex) {
		this.objRefs = objRefs;
		this.relationIndex = relationIndex;
	}

	@Override
	public void restore(Object target) {
		((IValueHolderContainer) target).set__Uninitialized(relationIndex, objRefs);
	}
}
