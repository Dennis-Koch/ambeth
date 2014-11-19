package de.osthus.esmeralda;

public class TypeResolveException extends RuntimeException
{
	public TypeResolveException(String fqTypeName)
	{
		super("Could not resolve '" + fqTypeName + "'");
	}
}
