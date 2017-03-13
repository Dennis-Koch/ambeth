package com.koch.ambeth.security;

public interface IServiceFilterExtendable
{
	void registerServiceFilter(IServiceFilter serviceFilter);

	void unregisterServiceFilter(IServiceFilter serviceFilter);
}
