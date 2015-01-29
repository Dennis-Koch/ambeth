package de.osthus.ambeth.ioc.exception;

public class BeanAlreadyDisposedException extends RuntimeException
{
	private static final long serialVersionUID = -8247312746220928267L;

	public BeanAlreadyDisposedException(String message)
	{
		super(message);
	}
}
