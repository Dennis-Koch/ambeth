package de.osthus.ambeth.sql;

import java.util.List;

import de.osthus.ambeth.appendable.IAppendable;

public interface ISqlBuilder
{
	String escapeName(CharSequence symbolName);

	IAppendable escapeName(CharSequence symbolName, IAppendable sb);

	String escapeSchemaAndSymbolName(CharSequence schemaName, CharSequence symbolName);

	IAppendable appendNameValue(CharSequence name, Object value, IAppendable sb);

	IAppendable appendNameValues(CharSequence name, List<Object> values, IAppendable sb);

	IAppendable appendName(CharSequence name, IAppendable sb);

	IAppendable appendValue(Object value, IAppendable sb);

	String escapeValue(CharSequence value);

	IAppendable escapeValue(CharSequence value, IAppendable sb);

	boolean isUnescapedType(Class<?> type);

	String[] getSchemaAndTableName(String tableName);
}
