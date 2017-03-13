package com.koch.ambeth.security.server.auth;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.security.AuthenticationException;
import com.koch.ambeth.security.AuthenticationResult;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.ICheckPasswordResult;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.security.server.IUserResolver;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class EmbeddedAuthenticationManager extends AbstractAuthenticationManager {
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

	@Property(name = SecurityServerConfigurationConstants.LoginPasswordAutoRehashActive,
			defaultValue = "true")
	protected boolean autoRehashPasswords;

	@Override
	public IAuthenticationResult authenticate(final IAuthentication authentication)
			throws AuthenticationException {
		IUser user;
		try {
			user = securityActivation
					.executeWithoutFiltering(new IResultingBackgroundWorkerDelegate<IUser>() {
						@Override
						public IUser invoke() throws Throwable {
							IUser user = userResolver.resolveUserBySID(authentication.getUserName());
							if (user != null) {
								// enforce loading
								user.getPassword();
							}
							return user;
						}
					});
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		if (user == null || user.getPassword() == null) {
			throw createAuthenticationException(authentication);
		}
		try {
			final IPassword password = user.getPassword();
			final ICheckPasswordResult checkPasswordResult =
					passwordUtil.checkClearTextPassword(authentication.getPassword(), password);
			if (!checkPasswordResult.isPasswordCorrect()) {
				throw createAuthenticationException(authentication);
			}
			boolean rehashRecommended = checkPasswordResult.isRehashPasswordRecommended();
			if (rehashRecommended && autoRehashPasswords) {
				securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>() {
					@Override
					public Object invoke() throws Throwable {
						passwordUtil.rehashPassword(authentication.getPassword(), password);
						return null;
					}
				});
				rehashRecommended = false;
			}
			String sid = userIdentifierProvider.getSID(user);
			return new AuthenticationResult(sid, checkPasswordResult.isChangePasswordRecommended(),
					rehashRecommended);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
