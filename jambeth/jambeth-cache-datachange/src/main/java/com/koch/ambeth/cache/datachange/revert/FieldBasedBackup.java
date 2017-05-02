package com.koch.ambeth.cache.datachange.revert;

import java.lang.reflect.Field;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class FieldBasedBackup implements IBackup {
	protected final Field[] fields;

	protected final Object[] values;

	public FieldBasedBackup(Field[] fields, Object[] values) {
		this.fields = fields;
		this.values = values;
	}

	@Override
	public void restore(Object target) {
		for (int b = fields.length; b-- > 0;) {
			Field field = fields[b];
			Object originalValue = values[b];
			try {
				field.set(target, originalValue);
			} catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}
}