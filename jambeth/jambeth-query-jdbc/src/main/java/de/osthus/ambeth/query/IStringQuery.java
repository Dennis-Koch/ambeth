package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

public interface IStringQuery
{
	boolean isJoinQuery();

	String fillQuery(List<Object> parameters);

	String fillQuery(Map<Object, Object> nameToValueMap, List<Object> parameters);

	String[] fillJoinQuery(Map<Object, Object> nameToValueMap, List<Object> parameters);
}
