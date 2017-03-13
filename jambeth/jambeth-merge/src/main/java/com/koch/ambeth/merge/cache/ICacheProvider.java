package com.koch.ambeth.merge.cache;

public interface ICacheProvider
{
	ICache getCurrentCache();

	boolean isNewInstanceOnCall();
}