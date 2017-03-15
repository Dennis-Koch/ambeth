package com.koch.ambeth.ioc.typeinfo;

import java.lang.reflect.Field;

import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class FieldPropertyInfoASM extends FieldPropertyInfo {
	protected final FieldAccess fieldAccess;

	protected final int index;

	protected IPropertyInfo fallbackPropertyInfo;

	public FieldPropertyInfoASM(Class<?> entityType, String propertyName, Field field,
			IThreadLocalObjectCollector objectCollector, FieldAccess fieldAccess) {
		super(entityType, propertyName, field, objectCollector);
		this.fieldAccess = fieldAccess;
		index = fieldAccess.getIndex(field.getName());
	}

	@Override
	public void setValue(Object obj, Object value) {
		try {
			if (fallbackPropertyInfo != null) {
				fallbackPropertyInfo.setValue(obj, value);
				return;
			}
			try {
				fieldAccess.set(obj, index, value);
			}
			catch (IllegalAccessError e) {
				fallbackPropertyInfo =
						new FieldPropertyInfo(getEntityType(), getName(), getBackingField(), null);
				fallbackPropertyInfo.setValue(obj, value);
				return;
			}
		}
		catch (Throwable e) {
			if (index == -1) {
				throw new UnsupportedOperationException(
						"No setter mapped while calling property '" + getName() + "' on object '" + obj
								+ "' of type '" + obj.getClass().toString() + "' with argument '" + value + "'");
			}
			throw RuntimeExceptionUtil.mask(e,
					"Error occured while setting '" + backingField + "' on object '" + obj + "' of type '"
							+ obj.getClass().toString() + "' with argument '" + value + "'");
		}
	}

	@Override
	public Object getValue(Object obj) {
		try {
			if (fallbackPropertyInfo != null) {
				return fallbackPropertyInfo.getValue(obj);
			}
			try {
				return fieldAccess.get(obj, index);
			}
			catch (IllegalAccessError e) {
				fallbackPropertyInfo =
						new FieldPropertyInfo(getEntityType(), getName(), getBackingField(), null);
				return fallbackPropertyInfo.getValue(obj);
			}
		}
		catch (Throwable e) {
			if (index == -1) {
				throw new UnsupportedOperationException();
			}
			throw RuntimeExceptionUtil.mask(e, "Error occured while getting '" + backingField
					+ "' on object '" + obj + "' of type '" + obj.getClass().toString() + "'");
		}
	}
}
