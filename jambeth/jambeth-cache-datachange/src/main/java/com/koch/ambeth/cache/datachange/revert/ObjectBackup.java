package com.koch.ambeth.cache.datachange.revert;

import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;

public class ObjectBackup implements IBackup {
	protected final ITypeInfoItem[] allMembers;

	protected final Object[] values;

	public ObjectBackup(ITypeInfoItem[] allMembers, Object[] values) {
		this.allMembers = allMembers;
		this.values = values;
	}

	@Override
	public void restore(Object target) {
		for (int b = allMembers.length; b-- > 0;) {
			ITypeInfoItem member = allMembers[b];
			Object originalValue = values[b];
			if (!EqualsUtil.equals(member.getValue(target), originalValue)) {
				member.setValue(target, originalValue);
			}
		}
	}
}