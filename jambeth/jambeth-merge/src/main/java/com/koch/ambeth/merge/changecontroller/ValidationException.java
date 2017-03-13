package com.koch.ambeth.merge.changecontroller;

/**
 * A validation exception is thrown if a validation (application logic) fails.
 */
public class ValidationException extends RuntimeException
{
	private static final long serialVersionUID = -4537210552824483170L;

	private final Object affectedEntity;

	public ValidationException(String message)
	{
		this(message, null);
	}

	public ValidationException(String message, Object entity)
	{
		super(message);
		affectedEntity = entity;
	}

	public Object getAffectedEntity()
	{
		return affectedEntity;
	}
}
