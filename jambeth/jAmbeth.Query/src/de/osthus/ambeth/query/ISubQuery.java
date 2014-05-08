package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ILinkedMap;

public interface ISubQuery<T>
{
	Class<?> getEntityType();

	String[] getSqlParts(Map<Object, Object> nameToValueMap, ILinkedMap<Integer, Object> params, List<String> additionalSelectColumnList);

	String getMainTableAlias();

	void setMainTableAlias(String alias);
}