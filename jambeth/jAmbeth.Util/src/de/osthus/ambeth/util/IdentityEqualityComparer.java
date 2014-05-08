package de.osthus.ambeth.util;

public class IdentityEqualityComparer<T>
{ // implements IEqualityComparer<T> {

	public boolean equals(T xKey, T yKey)
	{
		return xKey == yKey;
	}

	public int hashCode(T key)
	{
		return key.hashCode();
	}
}
