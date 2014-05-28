package de.osthus.ambeth.orm20.independent;

import java.util.List;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityA extends AbstractEntity
{
	private String name;

	private String name2;

	private List<Integer> values;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName2()
	{
		return name2;
	}

	public void setName2(String name2)
	{
		this.name2 = name2;
	}

	public List<Integer> getValues()
	{
		return values;
	}

	public void setValues(List<Integer> values)
	{
		this.values = values;
	}
}
