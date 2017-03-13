package com.koch.ambeth.query.jdbc;

import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public interface IStringQuery
{
	boolean isJoinQuery();

	String fillQuery(IList<Object> parameters);

	String fillQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters);

	String[] fillJoinQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters);
}
