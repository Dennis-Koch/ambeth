package com.koch.ambeth.persistence.jdbc.splitloading;

import com.koch.ambeth.model.AbstractEntity;

public class RefEntity extends AbstractEntity
{
	protected RefEntity other;

	protected RefEntity()
	{
		// Intended blank
	}

	public RefEntity getOther()
	{
		return other;
	}

	public void setOther(RefEntity other)
	{
		this.other = other;
	}
}
