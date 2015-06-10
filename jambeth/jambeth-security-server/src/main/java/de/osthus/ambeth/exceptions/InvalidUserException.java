package de.osthus.ambeth.exceptions;

public class InvalidUserException extends SecurityException
{
	private static final long serialVersionUID = -1163535828252169857L;

	private final String sid;

	public InvalidUserException(String sid)
	{
		super("User '" + sid + "' is not a valid user.");
		this.sid = sid;
	}

	public String getSID()
	{
		return sid;
	}
}
