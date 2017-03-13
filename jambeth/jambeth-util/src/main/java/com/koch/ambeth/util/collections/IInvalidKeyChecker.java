package com.koch.ambeth.util.collections;


public interface IInvalidKeyChecker<K>
{
	boolean isKeyValid(K key);
}
