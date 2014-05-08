package de.osthus.ambeth.filter.model;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IPagingRequest
{
	int getNumber();

	int getSize();
}