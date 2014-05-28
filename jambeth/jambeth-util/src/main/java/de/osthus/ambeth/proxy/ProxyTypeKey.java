package de.osthus.ambeth.proxy;

import java.util.Arrays;

public class ProxyTypeKey
{
	private final Class<?>[] interfaces;

	private final Class<?> baseType;

	public ProxyTypeKey(Class<?> baseType, Class<?>[] interfaces)
	{
		this.interfaces = interfaces;
		this.baseType = baseType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof ProxyTypeKey))
		{
			return false;
		}
		ProxyTypeKey other = (ProxyTypeKey) obj;
		return Arrays.equals(interfaces, other.interfaces) && baseType.equals(other.baseType);
	}

	@Override
	public int hashCode()
	{
		return baseType.hashCode() ^ Arrays.hashCode(interfaces);
	}
}
