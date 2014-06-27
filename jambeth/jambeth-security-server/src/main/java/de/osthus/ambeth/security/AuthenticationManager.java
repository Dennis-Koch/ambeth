package de.osthus.ambeth.security;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class AuthenticationManager implements IAuthenticationManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IUserResolver userResolver;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Property(name = IocConfigurationConstants.DebugModeActive, defaultValue = "false")
	protected boolean debugModeActive;

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
			final String userName = authentication.getUserName();
			final boolean fRehashRecommended = rehashRecommended;
			return new IAuthenticationResult()
			{
				@Override
				public boolean isChangePasswordRecommended()
				{
					return checkPasswordResult.isChangePasswordRecommended();
				}

				@Override
				public boolean isRehashPasswordRecommended()
				{
					return fRehashRecommended;
				}

				@Override
				public String getUserName()
				{
					return userName;
				}
			};
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected AuthenticationException createAuthenticationException(IAuthentication authentication)
	{
		// flush the stacktrace so that it can not be reconstructed whether the user existed or why specifically the authentication failed
		// due to security reasons because the source code and its knowledge around it is considered unsafe
		AuthenticationException e = new AuthenticationException("User '" + authentication.getUserName() + "' not found or credentials not valid");
		if (!debugModeActive)
		{
			e.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
		}
		return e;
	}
}
