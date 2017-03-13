package com.koch.ambeth.merge;

public interface IMergeExtension
{
	boolean handlesType(Class<?> type);

	boolean equalsObjects(Object left, Object right);

	Object extractPrimitiveValueToMerge(Object value);
}
