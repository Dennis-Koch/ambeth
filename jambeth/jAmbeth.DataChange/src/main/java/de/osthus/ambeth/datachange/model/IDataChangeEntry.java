package de.osthus.ambeth.datachange.model;

import de.osthus.ambeth.annotation.XmlType;

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