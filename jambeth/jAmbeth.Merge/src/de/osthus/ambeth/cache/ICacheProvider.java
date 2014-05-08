package de.osthus.ambeth.cache;

public interface ICacheProvider
{
	ICache getCurrentCache();

	boolean isNewInstanceOnCall();
}