package com.koch.ambeth.persistence.jdbc.mapping.models;

import java.util.List;

import com.koch.ambeth.model.AbstractEntity;

public class SelfReferencingEntityVO extends AbstractEntity
{
	protected String name;

	protected List<String> values;

	protected StringListType values2List;

	protected String relation1;

	protected String relation2;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public List<String> getValues()
	{
		return values;
	}

	public void setValues(List<String> values)
	{
		this.values = values;
	}

	public StringListType getValues2List()
	{
		return values2List;
	}

	public void setValues2List(StringListType values2List)
	{
		this.values2List = values2List;
	}

	public String getRelation1()
	{
		return relation1;
	}

	public void setRelation1(String relation1)
	{
		this.relation1 = relation1;
	}

	public String getRelation2()
	{
		return relation2;
	}

	public void setRelation2(String relation2)
	{
		this.relation2 = relation2;
	}

}
