package de.osthus.ambeth.filter.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum LogicalOperator
{
	/**
	 * OR logic for the combination of filters
	 * 
	 */
	@XmlEnumValue("Or")
	OR("Or"),

	/**
	 * AND logic for the combination of filters
	 * 
	 */
	@XmlEnumValue("And")
	AND("And");

	private final String value;

	LogicalOperator(String v)
	{
		value = v;
	}

	public String value()
	{
		return value;
	}
}
