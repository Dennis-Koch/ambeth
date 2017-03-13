package com.koch.ambeth.ioc;

public interface IModuleProvider
{
	Class<?>[] getFrameworkModules();

	Class<?>[] getBootstrapModules();
}
