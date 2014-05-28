package de.osthus.ambeth.persistence.xml.model;

import javax.persistence.Embeddable;

import de.osthus.ambeth.model.AbstractEntity;

@Embeddable
public class TestEmbeddedType extends AbstractEntity
{
	protected String name;

	protected int value;

	protected TestEmbeddedType()
	{
		// Intended blank
	}

	public String getName()
	{
		return name;
	}

	public int getValue()
	{
		return value;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setValue(int value)
	{
		this.value = value;
	}
}
