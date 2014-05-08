package de.osthus.ambeth.persistence.jdbc.splitloading;

import de.osthus.ambeth.model.AbstractEntity;

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
