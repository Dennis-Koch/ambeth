package com.koch.ambeth.filter;

import javax.xml.bind.annotation.XmlType;

@XmlType
public interface ISortDescriptor
{
	String getMember();

	SortDirection getSortDirection();
}