package com.koch.ambeth.cache.datachange.revert;

import java.util.Objects;

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
			Object originalValue = values[b];
			if (originalValue instanceof IBackup) {
				((IBackup) originalValue).restore(target);
				continue;
			}
			ITypeInfoItem member = allMembers[b];
			if (!Objects.equals(member.getValue(target), originalValue)) {
				member.setValue(target, originalValue);
			}
		}
	}
}
