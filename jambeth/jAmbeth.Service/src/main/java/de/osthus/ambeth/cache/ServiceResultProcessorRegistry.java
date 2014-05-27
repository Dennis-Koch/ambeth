package de.osthus.ambeth.cache;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.extendable.IMapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ServiceResultProcessorRegistry implements IInitializingBean, IServiceResultProcessorExtendable, IServiceResultProcessorRegistry
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IMapExtendableContainer<Class<?>, IServiceResultProcessor> extensions;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		extensions = new ClassExtendableContainer<IServiceResultProcessor>("serviceResultProcessor", "returnType");
	}

	@Override
	public IServiceResultProcessor getServiceResultProcessor(Class<?> returnType)
	{
		return extensions.getExtension(returnType);
	}

	@Override
	public void registerServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Class<?> returnType)
	{
		extensions.register(serviceResultProcessor, returnType);
	}

	@Override
	public void unregisterServiceResultProcessor(IServiceResultProcessor serviceResultProcessor, Class<?> returnType)
	{
		extensions.unregister(serviceResultProcessor, returnType);
	}
}
