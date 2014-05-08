package de.osthus.ambeth.ioc;

public interface IModuleProvider
{
	Class<?>[] getFrameworkModules();

	Class<?>[] getBootstrapModules();
}
