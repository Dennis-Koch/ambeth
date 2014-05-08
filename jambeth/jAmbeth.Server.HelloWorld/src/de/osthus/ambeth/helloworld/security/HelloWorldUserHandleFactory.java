package de.osthus.ambeth.helloworld.security;

import java.util.regex.Pattern;

import de.osthus.ambeth.helloworld.service.IHelloWorldService;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.security.IUseCase;
import de.osthus.ambeth.security.IUserHandle;
import de.osthus.ambeth.security.IUserHandleFactory;
import de.osthus.ambeth.security.UsecaseApplyType;

public class HelloWorldUserHandleFactory implements IUserHandleFactory, IInitializingBean
{
	protected final Pattern allowAllPattern = Pattern.compile(".*");

	protected final Pattern denyForbiddenMethodPattern = Pattern.compile(IHelloWorldService.class.getName().replaceAll("\\.", "\\\\.") + "\\.forbiddenMethod");

	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public IUserHandle createUserHandle(final String sid, final ISecurityScope[] securityScopes)
	{
		// Allow all service methods
		final Pattern[] allowPatterns = new Pattern[] { allowAllPattern };

		final Pattern[] denyPatterns = new Pattern[] { denyForbiddenMethodPattern };

		final IUseCase[] usecases = new IUseCase[] { new IUseCase()
		{
			@Override
			public Pattern[] getPatterns()
			{
				return allowPatterns;
			}

			@Override
			public UsecaseApplyType getApplyType()
			{
				return UsecaseApplyType.ALLOW;
			}

		}, new IUseCase()
		{
			@Override
			public Pattern[] getPatterns()
			{
				return denyPatterns;
			}

			@Override
			public UsecaseApplyType getApplyType()
			{
				return UsecaseApplyType.DENY;
			}

		} };

		return new IUserHandle()
		{
			@Override
			public boolean isValid()
			{
				return true;
			}

			@Override
			public IUseCase[] getUseCases()
			{
				return usecases;
			}

			@Override
			public String getSID()
			{
				return sid;
			}

			@Override
			public ISecurityScope[] getSecurityScopes()
			{
				return securityScopes;
			}
		};
	}
}
