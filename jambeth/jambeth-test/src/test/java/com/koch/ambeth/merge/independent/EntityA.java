package com.koch.ambeth.merge.independent;

import java.util.List;

public class EntityA extends BaseEntity
{
	protected EntityA other;

	protected List<EntityB> bs;

	protected EntityA()
	{
		// Intended blank
	}

	public EntityA getOther()
	{
		return other;
	}

	public void setOther(EntityA other)
	{
		this.other = other;
	}

	public List<EntityB> getBs()
	{
		return bs;
	}

	public void setBs(List<EntityB> bs)
	{
		this.bs = bs;
	}
}
