package de.osthus.ambeth.persistence;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.collections.IList;

public interface IPersistenceHelper
{
	IList<String> buildStringListOfValues(List<?> ids);

	String buildStringOfValues(List<?> ids);

	StringBuilder appendStringOfValues(List<?> ids, StringBuilder sb);

	IList<IList<Object>> splitValues(List<?> ids);

	IList<IList<Object>> splitValues(List<?> values, int batchSize);

	IList<IList<Object>> splitValues(Collection<?> ids);

	StringBuilder appendSplittedValues(String idColumnName, Class<?> fieldType, List<?> ids, List<Object> parameters, StringBuilder sb);
}
