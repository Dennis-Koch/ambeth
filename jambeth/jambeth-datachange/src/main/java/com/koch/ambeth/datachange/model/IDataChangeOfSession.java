package com.koch.ambeth.datachange.model;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IDataChangeOfSession
{
	long getSessionId();

	IDataChange getDataChange();
}