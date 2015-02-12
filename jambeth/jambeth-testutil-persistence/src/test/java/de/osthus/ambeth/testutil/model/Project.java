package de.osthus.ambeth.testutil.model;

import de.osthus.ambeth.model.AbstractEntity;

public class Project extends AbstractEntity
{
	public static final String Name = "Name";

	protected Integer alternateKey;

	protected String name;

	public void setAlternateKey(Integer alternateKey)
	{
		this.alternateKey = alternateKey;
	}

	public Integer getAlternateKey()
	{
		return alternateKey;
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
