package de.osthus.ambeth.change;

public class IllegalCommandException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public IllegalCommandException()
	{
		super();
	}

	public IllegalCommandException(String message)
	{
		super(message);
	}
}
