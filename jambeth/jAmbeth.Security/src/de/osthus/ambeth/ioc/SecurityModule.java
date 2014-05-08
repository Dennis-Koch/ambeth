package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.SecurityActivation;
import de.osthus.ambeth.security.SecurityScopeProvider;

@FrameworkModule
public class SecurityModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("securityActivation", SecurityActivation.class).autowireable(ISecurityActivation.class);

		beanContextFactory.registerBean("securityScopeProvider", SecurityScopeProvider.class).autowireable(ISecurityScopeProvider.class);
	}
}
