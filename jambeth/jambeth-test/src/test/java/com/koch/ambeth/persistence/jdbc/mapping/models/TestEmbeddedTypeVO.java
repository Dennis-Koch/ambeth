package com.koch.ambeth.persistence.jdbc.mapping.models;

import javax.persistence.Embeddable;

import com.koch.ambeth.model.AbstractEntity;

@Embeddable
public class TestEmbeddedTypeVO extends AbstractEntity
{
	protected String name;

	protected int value;

	public String getNameString()
	{
		return name;
	}

	public void setNameString(String name)
	{
		this.name = name;
	}

	public int getValueNumber()
	{
		return value;
	}

	public void setValueNumber(int value)
	{
		this.value = value;
	}
}
