package com.koch.ambeth.merge.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IMergeListenerExtendable;
import com.koch.ambeth.merge.changecontroller.AbstractRule;
import com.koch.ambeth.merge.changecontroller.ChangeController;
import com.koch.ambeth.merge.changecontroller.IChangeController;
import com.koch.ambeth.merge.changecontroller.IChangeControllerExtendable;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;

@FrameworkModule
public class ChangeControllerModule implements IInitializingModule
{
	@Property(name = MergeConfigurationConstants.edblActive, defaultValue = "true")
	protected Boolean edblActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (Boolean.TRUE.equals(edblActive))
		{
			IBeanConfiguration ccBean = beanContextFactory.registerAnonymousBean(ChangeController.class);
			ccBean.autowireable(IChangeController.class, IChangeControllerExtendable.class);
			beanContextFactory.link(ccBean).to(IMergeListenerExtendable.class);
		}

	}

	public static <T> IBeanConfiguration registerRule(IBeanContextFactory contextFactory, Class<? extends AbstractRule<T>> validatorClass,
			Class<T> validatedEntity)
	{
		IBeanConfiguration beanConfig = contextFactory.registerBean(validatorClass);
		contextFactory.link(beanConfig).to(IChangeControllerExtendable.class).with(validatedEntity);
		return beanConfig;
	}

}
