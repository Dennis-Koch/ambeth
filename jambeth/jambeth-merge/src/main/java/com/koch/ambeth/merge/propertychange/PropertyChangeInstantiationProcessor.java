package com.koch.ambeth.merge.propertychange;

import java.util.List;

import com.koch.ambeth.ioc.IBeanInstantiationProcessor;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.annotation.PropertyChangeAspect;

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
