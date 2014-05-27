package de.osthus.ambeth.persistence.xml.model;

import de.osthus.ambeth.model.AbstractEntity;

public class ProjectType extends AbstractEntity
{
	protected String name;

	protected ProjectType()
	{
		// Intended blank
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
