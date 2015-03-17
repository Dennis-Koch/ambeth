package de.osthus.ambeth.collections;


public interface IInvalidKeyChecker<K>
{
	boolean isKeyValid(K key);
}
