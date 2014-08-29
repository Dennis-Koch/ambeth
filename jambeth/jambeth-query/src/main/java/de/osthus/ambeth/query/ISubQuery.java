package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

public interface ISubQuery<T>
{
	Class<?> getEntityType();

	String[] getSqlParts(Map<Object, Object> nameToValueMap, List<Object> parameters, List<String> additionalSelectColumnList);

	String getMainTableAlias();

	void setMainTableAlias(String alias);
}