package de.osthus.ambeth.query;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;

public interface IStringQuery
{
	boolean isJoinQuery();

	String fillQuery(IList<Object> parameters);

	String fillQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters);

	String[] fillJoinQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters);
}
