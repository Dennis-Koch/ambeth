package de.osthus.ambeth.testutil;

import de.osthus.ambeth.model.ISecurityScope;

public class StringSecurityScope implements ISecurityScope
{
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
