package de.osthus.ambeth.exceptions;


public class PasswordConstraintException extends SecurityException
{
	private static final long serialVersionUID = -1163535828252169857L;

	public PasswordConstraintException(String message)
	{
		super(message);
	}
}
