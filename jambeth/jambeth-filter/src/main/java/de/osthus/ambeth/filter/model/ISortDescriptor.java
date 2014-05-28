package de.osthus.ambeth.filter.model;

import javax.xml.bind.annotation.XmlType;

@XmlType
public interface ISortDescriptor
{
	String getMember();

	SortDirection getSortDirection();
}