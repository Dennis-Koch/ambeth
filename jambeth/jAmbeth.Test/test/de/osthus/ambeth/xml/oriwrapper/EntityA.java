package de.osthus.ambeth.xml.oriwrapper;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.model.AbstractEntity;

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
