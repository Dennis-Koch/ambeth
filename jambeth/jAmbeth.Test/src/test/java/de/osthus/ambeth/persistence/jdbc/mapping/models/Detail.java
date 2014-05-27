package de.osthus.ambeth.persistence.jdbc.mapping.models;

import de.osthus.ambeth.model.AbstractEntity;

public class Detail extends AbstractEntity
{
	protected Master master;

	protected Detail()
	{
		// Intended blank
	}

	public Master getMaster()
	{
		return master;
	}

	public void setMaster(Master master)
	{
		this.master = master;
	}
}
