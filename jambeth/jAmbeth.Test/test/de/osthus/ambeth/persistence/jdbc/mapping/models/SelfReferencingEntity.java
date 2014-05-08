package de.osthus.ambeth.persistence.jdbc.mapping.models;

import java.util.Set;

import de.osthus.ambeth.model.AbstractEntity;

public class SelfReferencingEntity extends AbstractEntity
{
	protected String name;

	protected String[] values;

	protected Set<String> values2;

	protected SelfReferencingEntity relation1;

	protected SelfReferencingEntity relation2;

	protected SelfReferencingEntity()
	{
		// Intended blank
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String[] getValues()
	{
		return values;
	}

	public void setValues(String[] values)
	{
		this.values = values;
	}

	public Set<String> getValues2()
	{
		return values2;
	}

	public void setValues2(Set<String> values2)
	{
		this.values2 = values2;
	}

	public SelfReferencingEntity getRelation1()
	{
		return relation1;
	}

	public void setRelation1(SelfReferencingEntity relation1)
	{
		this.relation1 = relation1;
	}

	public SelfReferencingEntity getRelation2()
	{
		return relation2;
	}

	public void setRelation2(SelfReferencingEntity relation2)
	{
		this.relation2 = relation2;
	}
}
