package com.koch.ambeth.filter;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IPagingRequest
{
	int getNumber();

	int getSize();
}