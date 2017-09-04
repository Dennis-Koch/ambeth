package com.koch.ambeth.cache.util;

import java.util.List;
import java.util.Set;

public interface IRelationalCollectionFactory {
	List<?> createList(Class<?> expectedType, Class<?> elementType);

	Set<?> createSet(Class<?> expectedType, Class<?> elementType);
}
