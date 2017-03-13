package com.koch.ambeth.persistence.jdbc.alternateid;

import java.util.List;

import javax.persistence.PrePersist;

import com.koch.ambeth.model.AbstractEntity;

public class AlternateIdEntity extends AbstractEntity
{
	protected String name;

	protected BaseEntity baseEntity;

	protected List<BaseEntity2> baseEntities2;

	protected AlternateIdEntity()
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

	public BaseEntity getBaseEntity()
	{
		return baseEntity;
	}

	public void setBaseEntity(BaseEntity baseEntity)
	{
		this.baseEntity = baseEntity;
	}

	public List<BaseEntity2> getBaseEntities2()
	{
		return baseEntities2;
	}

	public void setBaseEntities2(List<BaseEntity2> baseEntities2)
	{
		this.baseEntities2 = baseEntities2;
	}

	@PrePersist
	public void prePersist()
	{
		if (getName() == null || getName().isEmpty())
		{
			setName(Long.toString(System.currentTimeMillis()));
		}
	}
}
