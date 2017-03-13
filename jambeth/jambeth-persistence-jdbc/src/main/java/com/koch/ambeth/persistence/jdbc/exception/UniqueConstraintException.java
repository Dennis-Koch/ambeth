package com.koch.ambeth.persistence.jdbc.exception;

public class UniqueConstraintException extends ConstraintException
{
	private static final long serialVersionUID = 7448945580470173927L;

	public UniqueConstraintException(String message, String relatedSql, Throwable e)
	{
		super(message, relatedSql, e);
	}
}
