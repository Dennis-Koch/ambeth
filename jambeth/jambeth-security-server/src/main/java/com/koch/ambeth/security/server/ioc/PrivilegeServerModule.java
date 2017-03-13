package com.koch.ambeth.security.server.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleProvider;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRuleProvider;
import com.koch.ambeth.security.server.service.PrivilegeService;
import com.koch.ambeth.security.service.IPrivilegeService;

@FrameworkModule
public class PrivilegeServerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("privilegeService", PrivilegeService.class).autowireable(//
				IPrivilegeService.class,//
				IEntityPermissionRuleExtendable.class, IEntityTypePermissionRuleExtendable.class,//
				IEntityPermissionRuleProvider.class, IEntityTypePermissionRuleProvider.class);

	}
}
