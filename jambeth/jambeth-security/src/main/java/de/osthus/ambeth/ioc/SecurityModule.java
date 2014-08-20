package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.security.IAuthorizationChangeListenerExtendable;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityScopeChangeListenerExtendable;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.SecurityActivation;
import de.osthus.ambeth.security.SecurityContextHolder;
import de.osthus.ambeth.security.SecurityScopeProvider;

@FrameworkModule
public class SecurityModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(SecurityActivation.class).autowireable(ISecurityActivation.class);

		beanContextFactory.registerAnonymousBean(SecurityScopeProvider.class).autowireable(ISecurityScopeProvider.class,
				ISecurityScopeChangeListenerExtendable.class);

		beanContextFactory.registerAnonymousBean(SecurityContextHolder.class).autowireable(SecurityContextHolder.class,
				IAuthorizationChangeListenerExtendable.class);
	}
}
