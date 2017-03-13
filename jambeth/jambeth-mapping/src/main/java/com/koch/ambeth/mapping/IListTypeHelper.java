package com.koch.ambeth.mapping;

import java.util.Collection;

public interface IListTypeHelper
{
	<L> L packInListType(Collection<?> referencedVOs, Class<L> listType);

	Object unpackListType(Object item);

	boolean isListType(Class<?> type);
}