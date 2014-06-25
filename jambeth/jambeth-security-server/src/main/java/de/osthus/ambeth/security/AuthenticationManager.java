package de.osthus.ambeth.security;

import java.util.Calendar;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class AuthenticationManager implements IAuthenticationManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IPasswordResolver passwordResolver;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Property(name = IocConfigurationConstants.DebugModeActive, defaultValue = "false")
	protected boolean debugModeActive;

	@Override
	public IAuthenticationResult authenticate(final IAuthentication authentication) throws AuthenticationException
	{
		IPassword password;
		try
		{
			password = securityActivation.executeWithoutFiltering(new IResultingBackgroundWorkerDelegate<IPassword>()
			{
				@Override
				public IPassword invoke() throws Throwable
				{
					return passwordResolver.resolvePassword(authentication);
				}
			});
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (password == null)
		{
			throw createAuthenticationException(authentication);
		}
		try
		{
			String passwordString = new String(password.getValue());
			String givenPasswordString = Base64.encodeBytes(passwordUtil.hashClearTextPassword(authentication.getPassword(), password));
			if (!passwordString.equals(givenPasswordString))
			{
				throw createAuthenticationException(authentication);
			}
			Calendar currentTime = Calendar.getInstance();
			final boolean changePassword = currentTime.after(password.getChangeAfter());
			if (changePassword)
			{
				if (log.isWarnEnabled())
				{
					log.warn("Password for user '" + authentication.getUserName() + "' is outdated. Please set a new password");
				}
			}
			return new IAuthenticationResult()
			{
				@Override
				public boolean isChangePassword()
				{
					return changePassword;
				}

				@Override
				public String getUserName()
				{
					return authentication.getUserName();
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
