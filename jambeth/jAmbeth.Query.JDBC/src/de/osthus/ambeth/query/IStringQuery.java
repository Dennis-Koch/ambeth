package de.osthus.ambeth.query;

import java.util.Map;

public interface IStringQuery
{
	boolean isJoinQuery();

	String fillQuery(Map<Integer, Object> params);

	String fillQuery(Map<Object, Object> nameToValueMap, Map<Integer, Object> params);

	String[] fillJoinQuery(Map<Object, Object> nameToValueMap, Map<Integer, Object> params);
}
