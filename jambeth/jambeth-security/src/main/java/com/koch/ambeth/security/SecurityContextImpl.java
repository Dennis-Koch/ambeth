package com.koch.ambeth.security;

public class SecurityContextImpl implements ISecurityContext
{
	protected IAuthentication authentication;

	protected IAuthorization authorization;

	protected final SecurityContextHolder securityContextHolder;

	public SecurityContextImpl(SecurityContextHolder securityContextHolder)
	{
		this.securityContextHolder = securityContextHolder;
	}

	@Override
	public void setAuthentication(IAuthentication authentication)
	{
		this.authentication = authentication;
	}

	@Override
	public IAuthentication getAuthentication()
	{
		return authentication;
	}

	@Override
	public void setAuthorization(IAuthorization authorization)
	{
		this.authorization = authorization;
		securityContextHolder.notifyAuthorizationChangeListeners(authorization);
	}

	@Override
	public IAuthorization getAuthorization()
	{
		return authorization;
	}

}