package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeSecurityManager;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.privilege.IEntityPermissionRule;
import de.osthus.ambeth.privilege.IEntityPermissionRuleExtendable;
import de.osthus.ambeth.privilege.IEntityTypePermissionRule;
import de.osthus.ambeth.privilege.IEntityTypePermissionRuleExtendable;
import de.osthus.ambeth.privilege.IPermissionRule;
import de.osthus.ambeth.security.AuthenticationManager;
import de.osthus.ambeth.security.DefaultServiceFilter;
import de.osthus.ambeth.security.IActionPermission;
import de.osthus.ambeth.security.IAuthenticationManager;
import de.osthus.ambeth.security.IPBEncryptor;
import de.osthus.ambeth.security.IPasswordUtil;
import de.osthus.ambeth.security.IPrivateKeyProvider;
import de.osthus.ambeth.security.ISecurityManager;
import de.osthus.ambeth.security.IServiceFilterExtendable;
import de.osthus.ambeth.security.ISignatureUtil;
import de.osthus.ambeth.security.PBEncryptor;
import de.osthus.ambeth.security.PasswordUtil;
import de.osthus.ambeth.security.PersistedPrivateKeyProvider;
import de.osthus.ambeth.security.SignatureUtil;
import de.osthus.ambeth.security.privilegeprovider.ActionPermissionRule;
import de.osthus.ambeth.security.proxy.SecurityPostProcessor;

@FrameworkModule
public class SecurityServerModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean isSecurityActive;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(PasswordUtil.class).autowireable(IPasswordUtil.class);
		beanContextFactory.registerBean(PBEncryptor.class).autowireable(IPBEncryptor.class);
		beanContextFactory.registerBean(SignatureUtil.class).autowireable(ISignatureUtil.class);
		beanContextFactory.registerBean(PersistedPrivateKeyProvider.class).autowireable(IPrivateKeyProvider.class);

		if (isSecurityActive)
		{
			beanContextFactory.registerBean(SecurityPostProcessor.class);

			beanContextFactory.registerBean(AuthenticationManager.class).autowireable(IAuthenticationManager.class);

			beanContextFactory.registerBean(de.osthus.ambeth.security.SecurityManager.class).autowireable(ISecurityManager.class, IMergeSecurityManager.class,
					IServiceFilterExtendable.class);

			registerAndLinkPermissionRule(beanContextFactory, ActionPermissionRule.class, IActionPermission.class);

			IBeanConfiguration defaultServiceFilterBC = beanContextFactory.registerBean(DefaultServiceFilter.class);
			beanContextFactory.link(defaultServiceFilterBC).to(IServiceFilterExtendable.class);
		}
	}

	public static void registerAndLinkPermissionRule(IBeanContextFactory beanContextFactory, Class<? extends IPermissionRule> permissionRuleType,
			Class<?>... entityTypes)
	{
		IBeanConfiguration permissionRule = beanContextFactory.registerBean(permissionRuleType);
		for (Class<?> entityType : entityTypes)
		{
			linkPermissionRule(beanContextFactory, permissionRule, entityType);
		}
	}

	public static void linkPermissionRule(IBeanContextFactory beanContextFactory, IBeanConfiguration entityOrEntityTypePermissionRule, Class<?> entityType)
	{
		boolean atLeastOneRegisterMatched = false;
		if (IEntityPermissionRule.class.isAssignableFrom(entityOrEntityTypePermissionRule.getBeanType()))
		{
			beanContextFactory.link(entityOrEntityTypePermissionRule).to(IEntityPermissionRuleExtendable.class).with(entityType);
			atLeastOneRegisterMatched = true;
		}
		if (IEntityTypePermissionRule.class.isAssignableFrom(entityOrEntityTypePermissionRule.getBeanType()))
		{
			beanContextFactory.link(entityOrEntityTypePermissionRule).to(IEntityTypePermissionRuleExtendable.class).with(entityType);
			atLeastOneRegisterMatched = true;
		}
		if (!atLeastOneRegisterMatched)
		{
			throw new IllegalArgumentException("Given bean does not implement any of the permission rule interfaces:"
					+ entityOrEntityTypePermissionRule.getBeanType());
		}
	}
}
