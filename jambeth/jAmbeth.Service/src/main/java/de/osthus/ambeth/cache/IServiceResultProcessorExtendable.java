package de.osthus.ambeth.cache;

public interface IServiceResultProcessorExtendable
{
	void registerServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Class<?> returnType);

	void unregisterServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Class<?> returnType);
}
