package com.koch.ambeth.merge;

public interface ICUDResultExtendable
{
	void registerCUDResultExtension(ICUDResultExtension cudResultExtension, Class<?> entityType);

	void unregisterCUDResultExtension(ICUDResultExtension cudResultExtension, Class<?> entityType);
}
