package com.koch.ambeth.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum SortDirection
{
	/**
	 * result is sorted descending
	 * 
	 */
	@XmlEnumValue("Descending")
	DESCENDING("Descending"),

	/**
	 * result is sorted ascending
	 * 
	 */
	@XmlEnumValue("Ascending")
	ASCENDING("Ascending");

	private final String value;

	SortDirection(String v)
	{
		value = v;
	}

	public String value()
	{
		return value;
	}
}
