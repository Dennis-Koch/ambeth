package de.osthus.ambeth.propertychange;

import java.util.List;

import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.annotation.PropertyChangeAspect;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.ioc.IBeanInstantiationProcessor;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class PropertyChangeInstantiationProcessor implements IBeanInstantiationProcessor
{
	public static interface CreateDelegate
	{
		Object create();
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Override
	public Object instantiateBean(BeanContextFactory beanContextFactory, ServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType,
			List<IBeanConfiguration> beanConfHierarchy)
	{
		if (!beanType.isAnnotationPresent(PropertyChangeAspect.class))
		{
			return null;
		}
		IBytecodeEnhancer bytecodeEnhancer = beanContext.getService(IBytecodeEnhancer.class);
		beanType = bytecodeEnhancer.getEnhancedType(beanType, PropertyChangeEnhancementHint.PropertyChangeEnhancementHint);
		return accessorTypeProvider.getConstructorType(CreateDelegate.class, beanType).create();
	}
}
