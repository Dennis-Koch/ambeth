package de.osthus.ambeth.persistence.jdbc.mapping.models;

import de.osthus.ambeth.model.AbstractEntity;

public class MasterVO extends AbstractEntity
{
	private DetailListVO details;

	public DetailListVO getDetails()
	{
		return details;
	}

	public void setDetails(DetailListVO details)
	{
		this.details = details;
	}
}
