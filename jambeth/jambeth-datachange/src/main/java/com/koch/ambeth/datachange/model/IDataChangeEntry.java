package com.koch.ambeth.datachange.model;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IDataChangeEntry
{
	Class<?> getEntityType();

	Object getId();

	byte getIdNameIndex();

	Object getVersion();

	String[] getTopics();

	void setTopics(String[] topics);
}