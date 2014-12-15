package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.security.IAuthorizationChangeListenerExtendable;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.SecurityActivation;
import de.osthus.ambeth.security.SecurityContextHolder;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.MultithreadingHelper;

@FrameworkModule
public class SecurityModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(MultithreadingHelper.class).autowireable(IMultithreadingHelper.class);

		beanContextFactory.registerBean(SecurityActivation.class).autowireable(ISecurityActivation.class);

		beanContextFactory.registerBean(SecurityContextHolder.class).autowireable(ISecurityContextHolder.class, IAuthorizationChangeListenerExtendable.class);
	}
}
