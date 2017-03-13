package com.koch.ambeth.persistence.xml.model;

import java.util.List;

public interface Group
{
	Integer getId();

	Integer getVersion();

	String getName();

	void setName(String name);

	List<Group> getChildGroups();

	List<Group> getParentGroups();

}
