package com.koch.ambeth.xml.oriwrapper;

import java.util.List;

import com.koch.ambeth.model.AbstractEntity;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public class EntityA extends AbstractEntity
{
	protected List<EntityB> entityBs;

	protected EntityA()
	{
		// Intended blank
	}

	public List<EntityB> getEntityBs()
	{
		return entityBs;
	}

	public void setEntityBs(List<EntityB> entityBs)
	{
		this.entityBs = entityBs;
	}
}
