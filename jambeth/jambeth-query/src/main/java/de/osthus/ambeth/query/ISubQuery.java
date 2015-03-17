package de.osthus.ambeth.query;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.util.IDisposable;

public interface ISubQuery<T> extends IDisposable
{
	Class<?> getEntityType();

	String[] getSqlParts(IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList);

	String getMainTableAlias();

	void setMainTableAlias(String alias);
}