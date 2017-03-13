package com.koch.ambeth.security.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.security.ILightweightSecurityContext;
import com.koch.ambeth.security.AuthenticatedUserHolder;
import com.koch.ambeth.security.IAuthenticatedUserHolder;
import com.koch.ambeth.security.IAuthorizationChangeListenerExtendable;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.SecurityContextHolder;

@FrameworkModule
public class SecurityModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(AuthenticatedUserHolder.class).autowireable(IAuthenticatedUserHolder.class);

		beanContextFactory.registerBean(SecurityContextHolder.class).autowireable(ISecurityContextHolder.class, IAuthorizationChangeListenerExtendable.class,
				ILightweightSecurityContext.class);
	}
}
