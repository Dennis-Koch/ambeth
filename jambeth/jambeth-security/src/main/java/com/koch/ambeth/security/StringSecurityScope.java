package com.koch.ambeth.security;

import com.koch.ambeth.service.model.ISecurityScope;

public class StringSecurityScope implements ISecurityScope
{
	public static final String DEFAULT_SCOPE_NAME = "defaultScope";

	public static final ISecurityScope DEFAULT_SCOPE = new StringSecurityScope(DEFAULT_SCOPE_NAME);

	protected final String name;

	public StringSecurityScope(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
