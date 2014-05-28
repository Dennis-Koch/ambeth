package de.osthus.ambeth.sql;

import java.util.List;

public interface ISqlBuilder
{
	Appendable appendNameValue(String name, Object value, Appendable sb);

	Appendable appendNameValues(String name, List<Object> values, Appendable sb);

	Appendable appendName(String name, Appendable sb);

	Appendable appendValue(Object value, Appendable sb);

	String escapeName(CharSequence name);

	String escapeValue(CharSequence value);

	<V extends Appendable> V escapeValue(CharSequence value, V sb);

	boolean isUnescapedType(Class<?> type);
}
