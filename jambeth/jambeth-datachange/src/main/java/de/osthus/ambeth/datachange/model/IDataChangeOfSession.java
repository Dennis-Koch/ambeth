package de.osthus.ambeth.datachange.model;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IDataChangeOfSession
{
	long getSessionId();

	IDataChange getDataChange();
}