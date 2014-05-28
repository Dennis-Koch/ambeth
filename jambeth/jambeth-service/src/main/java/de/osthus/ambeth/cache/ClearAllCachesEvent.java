package de.osthus.ambeth.cache;

public final class ClearAllCachesEvent
{
	protected static final ClearAllCachesEvent instance = new ClearAllCachesEvent();

	public static ClearAllCachesEvent getInstance()
	{
		return instance;
	}

	private ClearAllCachesEvent()
	{
		// intended blank
	}
}
