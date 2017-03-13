package com.koch.ambeth.util.exception;

public class EnumNotSupportedException extends IllegalStateException
{
	private static final long serialVersionUID = -7336332572216860033L;

	private final Enum<?> enumInstance;

	public EnumNotSupportedException(Enum<?> enumInstance)
	{
		super("Enum not supported: " + enumInstance);
		this.enumInstance = enumInstance;
	}

	public Enum<?> getEnumInstance()
	{
		return enumInstance;
	}
}
