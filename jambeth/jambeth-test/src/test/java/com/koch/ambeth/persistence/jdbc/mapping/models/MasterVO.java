package com.koch.ambeth.persistence.jdbc.mapping.models;

import com.koch.ambeth.model.AbstractEntity;

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
