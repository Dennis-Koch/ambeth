package de.osthus.ambeth.persistence.jdbc.mapping.models;

import java.util.List;

import de.osthus.ambeth.model.AbstractEntity;

public class Master extends AbstractEntity
{
	protected List<Detail> details;

	protected Master()
	{
		// Intended blank
	}

	public List<Detail> getDetails()
	{
		return details;
	}

	public void setDetails(List<Detail> details)
	{
		this.details = details;
	}
}
