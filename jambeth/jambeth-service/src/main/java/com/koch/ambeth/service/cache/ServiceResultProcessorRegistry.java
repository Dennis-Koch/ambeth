package com.koch.ambeth.service.cache;

import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.extendable.IMapExtendableContainer;

public class ServiceResultProcessorRegistry
		implements IServiceResultProcessorExtendable, IServiceResultProcessorRegistry {
	protected final IMapExtendableContainer<Class<?>, IServiceResultProcessor> extensions = new ClassExtendableContainer<>(
			"serviceResultProcessor", "returnType");

	@Override
	public IServiceResultProcessor getServiceResultProcessor(Class<?> returnType) {
		return extensions.getExtension(returnType);
	}

	@Override
	public void registerServiceResultProcessor(IServiceResultProcessor serviceResultProcessor,
			Class<?> returnType) {
		extensions.register(serviceResultProcessor, returnType);
	}

	@Override
	public void unregisterServiceResultProcessor(IServiceResultProcessor serviceResultProcessor,
			Class<?> returnType) {
		extensions.unregister(serviceResultProcessor, returnType);
	}
}
