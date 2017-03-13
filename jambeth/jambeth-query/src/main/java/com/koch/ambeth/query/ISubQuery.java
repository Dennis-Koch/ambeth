package com.koch.ambeth.query;

import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public interface ISubQuery<T> extends IDisposable
{
	Class<?> getEntityType();

	String[] getSqlParts(IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList);

	String getMainTableAlias();

	void setMainTableAlias(String alias);
}