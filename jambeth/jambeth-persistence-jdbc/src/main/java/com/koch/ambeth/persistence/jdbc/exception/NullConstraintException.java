package com.koch.ambeth.persistence.jdbc.exception;

public class NullConstraintException extends ConstraintException
{
	private static final long serialVersionUID = 7448945580470173927L;

	public NullConstraintException(String message, String relatedSql, Throwable e)
	{
		super(message, relatedSql, e);
	}
}
