package de.osthus.esmeralda;

public class TypeResolveException extends RuntimeException
{
	private static final long serialVersionUID = 7756437006223012561L;

	public TypeResolveException(String fqTypeName)
	{
		super("Could not resolve '" + fqTypeName + "'");
	}
}
