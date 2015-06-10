package de.osthus.ambeth.security.auth;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.AuthenticationException;
import de.osthus.ambeth.security.AuthenticationResult;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthenticationResult;
import de.osthus.ambeth.security.ICheckPasswordResult;
import de.osthus.ambeth.security.IPasswordUtil;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.IUserIdentifierProvider;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class EmbeddedAuthenticationManager extends AbstractAuthenticationManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IUserIdentifierProvider userIdentifierProvider;

	@Autowired
	protected IUserResolver userResolver;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAutoRehashActive, defaultValue = "true")
	protected boolean autoRehashPasswords;

	@Override
	public IAuthenticationResult authenticate(final IAuthentication authentication) throws AuthenticationException
	{
		IUser user;
		try
		{
			user = securityActivation.executeWithoutFiltering(new IResultingBackgroundWorkerDelegate<IUser>()
			{
				@Override
				public IUser invoke() throws Throwable
				{
					IUser user = userResolver.resolveUserBySID(authentication.getUserName());
					if (user != null)
					{
						// enforce loading
						user.getPassword();
					}
					return user;
				}
			});
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (user == null || user.getPassword() == null)
		{
			throw createAuthenticationException(authentication);
		}
		try
		{
			final IPassword password = user.getPassword();
			final ICheckPasswordResult checkPasswordResult = passwordUtil.checkClearTextPassword(authentication.getPassword(), password);
			if (!checkPasswordResult.isPasswordCorrect())
			{
				throw createAuthenticationException(authentication);
			}
			boolean rehashRecommended = checkPasswordResult.isRehashPasswordRecommended();
			if (rehashRecommended && autoRehashPasswords)
			{
				securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
				{
					@Override
					public Object invoke() throws Throwable
					{
						passwordUtil.rehashPassword(authentication.getPassword(), password);
						return null;
					}
				});
				rehashRecommended = false;
			}
			String sid = userIdentifierProvider.getSID(user);
			return new AuthenticationResult(sid, checkPasswordResult.isChangePasswordRecommended(), rehashRecommended);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
