package de.osthus.ambeth.persistence;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;

public interface IPersistenceHelper
{
	IList<String> buildStringListOfValues(List<?> ids);

	String buildStringOfValues(List<?> ids);

	IAppendable appendStringOfValues(List<?> ids, IAppendable sb);

	IList<IList<Object>> splitValues(List<?> ids);

	IList<IList<Object>> splitValues(List<?> values, int batchSize);

	IList<IList<Object>> splitValues(Collection<?> ids);

	IAppendable appendSplittedValues(String idColumnName, Class<?> fieldType, List<?> ids, List<Object> parameters, IAppendable sb);
}
