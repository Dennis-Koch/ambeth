package de.osthus.ambeth.query;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;

public interface ISubQuery<T>
{
	Class<?> getEntityType();

	String[] getSqlParts(IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList);

	String getMainTableAlias();

	void setMainTableAlias(String alias);
}