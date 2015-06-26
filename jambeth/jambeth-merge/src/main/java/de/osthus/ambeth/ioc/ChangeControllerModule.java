package de.osthus.ambeth.ioc;

import de.osthus.ambeth.changecontroller.ChangeController;
import de.osthus.ambeth.changecontroller.IChangeController;
import de.osthus.ambeth.changecontroller.IChangeControllerExtendable;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IMergeListenerExtendable;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;

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
}
