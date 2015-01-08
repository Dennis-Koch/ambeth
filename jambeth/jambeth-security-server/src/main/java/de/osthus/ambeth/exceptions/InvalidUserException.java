package de.osthus.ambeth.exceptions;

public class InvalidUserException extends SecurityException
{
	private static final long serialVersionUID = -1163535828252169857L;

	public InvalidUserException(String userName, String sid)
	{
		super("User is not a valid user. '" + userName + "' with SID '" + sid + "'");
	}
}
