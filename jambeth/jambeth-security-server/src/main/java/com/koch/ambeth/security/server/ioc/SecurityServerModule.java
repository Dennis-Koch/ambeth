package com.koch.ambeth.security.server.ioc;

/*-
 * #%L
 * jambeth-security-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IMergeSecurityManager;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.security.IActionPermission;
import com.koch.ambeth.security.IAuthenticationManager;
import com.koch.ambeth.security.ISecurityManager;
import com.koch.ambeth.security.IServiceFilterExtendable;
import com.koch.ambeth.security.events.AuthorizationMissingEvent;
import com.koch.ambeth.security.server.AuthorizationProcess;
import com.koch.ambeth.security.server.DefaultServiceFilter;
import com.koch.ambeth.security.server.IAuthorizationProcess;
import com.koch.ambeth.security.server.IPBEncryptor;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.security.server.IPasswordValidationExtendable;
import com.koch.ambeth.security.server.IPrivateKeyProvider;
import com.koch.ambeth.security.server.ISecureRandom;
import com.koch.ambeth.security.server.ISignatureUtil;
import com.koch.ambeth.security.server.OnDemandSecureRandom;
import com.koch.ambeth.security.server.PBEncryptor;
import com.koch.ambeth.security.server.PasswordUtil;
import com.koch.ambeth.security.server.PersistedPrivateKeyProvider;
import com.koch.ambeth.security.server.SignatureUtil;
import com.koch.ambeth.security.server.auth.AuthenticationResultCache;
import com.koch.ambeth.security.server.auth.EmbeddedAuthenticationManager;
import com.koch.ambeth.security.server.auth.IAuthenticationResultCache;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRule;
import com.koch.ambeth.security.server.privilege.IEntityTypePermissionRuleExtendable;
import com.koch.ambeth.security.server.privilege.IPermissionRule;
import com.koch.ambeth.security.server.privilegeprovider.ActionPermissionRule;
import com.koch.ambeth.security.server.proxy.SecurityPostProcessor;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

@FrameworkModule
public class SecurityServerModule implements IInitializingModule {
	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean isSecurityActive;

	@Property(name = SecurityServerConfigurationConstants.AuthenticationManagerType, mandatory = false)
	protected Class<?> authenticationManagerType;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(PasswordUtil.class).autowireable(IPasswordUtil.class,
				IPasswordValidationExtendable.class);
		beanContextFactory.registerBean(PBEncryptor.class).autowireable(IPBEncryptor.class);
		beanContextFactory.registerBean(SignatureUtil.class).autowireable(ISignatureUtil.class);
		beanContextFactory.registerBean(PersistedPrivateKeyProvider.class)
				.autowireable(IPrivateKeyProvider.class);

		beanContextFactory.registerBean(OnDemandSecureRandom.class).autowireable(ISecureRandom.class);

		if (isSecurityActive) {
			IBeanConfiguration authorizationProcess = beanContextFactory
					.registerBean(AuthorizationProcess.class).autowireable(IAuthorizationProcess.class);
			beanContextFactory
					.link(authorizationProcess, AuthorizationProcess.HANDLE_AUTHORIZATION_MSSING)
					.to(IEventListenerExtendable.class).with(AuthorizationMissingEvent.class);

			beanContextFactory.registerBean(SecurityPostProcessor.class);

			if (authenticationManagerType == null) {
				authenticationManagerType = EmbeddedAuthenticationManager.class;
			}

			// Check to Object.class is a "feature" to allow to fully customize the bean definition from
			// another module
			if (authenticationManagerType != Object.class) {
				beanContextFactory.registerBean(authenticationManagerType)
						.autowireable(IAuthenticationManager.class);
			}
			IBeanConfiguration authenticationResultCache = beanContextFactory
					.registerBean(AuthenticationResultCache.class)
					.autowireable(IAuthenticationResultCache.class);
			beanContextFactory
					.link(authenticationResultCache,
							AuthenticationResultCache.DELEGATE_HANDLE_CLEAR_ALL_CACHES_EVENT)
					.to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

			beanContextFactory.registerBean(com.koch.ambeth.security.server.SecurityManager.class)
					.autowireable(ISecurityManager.class, IMergeSecurityManager.class,
							IServiceFilterExtendable.class);

			registerAndLinkPermissionRule(beanContextFactory, ActionPermissionRule.class,
					IActionPermission.class);

			IBeanConfiguration defaultServiceFilterBC = beanContextFactory
					.registerBean(DefaultServiceFilter.class);
			beanContextFactory.link(defaultServiceFilterBC).to(IServiceFilterExtendable.class);
		}
	}

	public static void registerAndLinkPermissionRule(IBeanContextFactory beanContextFactory,
			Class<? extends IPermissionRule> permissionRuleType, Class<?>... entityTypes) {
		IBeanConfiguration permissionRule = beanContextFactory.registerBean(permissionRuleType);
		for (Class<?> entityType : entityTypes) {
			linkPermissionRule(beanContextFactory, permissionRule, entityType);
		}
	}

	public static void linkPermissionRule(IBeanContextFactory beanContextFactory,
			IBeanConfiguration entityOrEntityTypePermissionRule, Class<?> entityType) {
		boolean atLeastOneRegisterMatched = false;
		if (IEntityPermissionRule.class
				.isAssignableFrom(entityOrEntityTypePermissionRule.getBeanType())) {
			beanContextFactory.link(entityOrEntityTypePermissionRule)
					.to(IEntityPermissionRuleExtendable.class).with(entityType);
			atLeastOneRegisterMatched = true;
		}
		if (IEntityTypePermissionRule.class
				.isAssignableFrom(entityOrEntityTypePermissionRule.getBeanType())) {
			beanContextFactory.link(entityOrEntityTypePermissionRule)
					.to(IEntityTypePermissionRuleExtendable.class).with(entityType);
			atLeastOneRegisterMatched = true;
		}
		if (!atLeastOneRegisterMatched) {
			throw new IllegalArgumentException(
					"Given bean does not implement any of the permission rule interfaces:"
							+ entityOrEntityTypePermissionRule.getBeanType());
		}
	}
}
