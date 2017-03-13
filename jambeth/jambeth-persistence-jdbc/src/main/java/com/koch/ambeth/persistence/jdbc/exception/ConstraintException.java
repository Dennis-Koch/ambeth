package com.koch.ambeth.persistence.jdbc.exception;

public abstract class ConstraintException extends AbstractAmbethPersistenceException
{
	private static final long serialVersionUID = 7448945580470173927L;

	protected ConstraintException(String message, String relatedSql, Throwable e)
	{
		super(message, relatedSql, e);
	}
}
