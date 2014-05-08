package de.osthus.ambeth.persistence.update;

import java.util.List;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityA extends AbstractEntity
{
	protected EntityB other;

	protected List<EntityC> entityCs;

	protected EntityD entityD;

	protected EntityA()
	{
		// Intended blank
	}

	public EntityB getOther()
	{
		return other;
	}

	public void setOther(EntityB other)
	{
		this.other = other;
	}

	public List<EntityC> getEntityCs()
	{
		return entityCs;
	}

	public void setEntityCs(List<EntityC> entityCs)
	{
		this.entityCs = entityCs;
	}

	public EntityD getEntityD()
	{
		return entityD;
	}

	public void setEntityD(EntityD entityD)
	{
		this.entityD = entityD;
	}
}
