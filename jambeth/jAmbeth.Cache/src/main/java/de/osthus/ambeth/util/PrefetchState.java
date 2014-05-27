package de.osthus.ambeth.util;

public class PrefetchState implements IPrefetchState
{
	@SuppressWarnings("unused")
	private final Object hardRef;

	public PrefetchState(Object hardRef)
	{
		this.hardRef = hardRef;
	}
}
