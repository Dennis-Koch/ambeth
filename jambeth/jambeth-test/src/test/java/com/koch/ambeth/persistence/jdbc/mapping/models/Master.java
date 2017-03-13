package com.koch.ambeth.persistence.jdbc.mapping.models;

import java.util.List;

import com.koch.ambeth.model.AbstractEntity;

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
