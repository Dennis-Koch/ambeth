package de.osthus.ambeth.sql;

import java.util.List;

import de.osthus.ambeth.appendable.IAppendable;

public interface ISqlBuilder
{
	IAppendable appendNameValue(String name, Object value, IAppendable sb);

	IAppendable appendNameValues(String name, List<Object> values, IAppendable sb);

	IAppendable appendName(String name, IAppendable sb);

	IAppendable appendValue(Object value, IAppendable sb);

	String escapeName(CharSequence name);

	IAppendable escapeName(CharSequence name, IAppendable sb);

	String escapeValue(CharSequence value);

	IAppendable escapeValue(CharSequence value, IAppendable sb);

	boolean isUnescapedType(Class<?> type);

	String[] getSchemaAndTableName(String tableName);
}
