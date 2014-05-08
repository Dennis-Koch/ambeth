package de.osthus.ambeth.orm;

import de.osthus.ambeth.util.ParamChecker;

public class IndependentLinkConfig extends LinkConfig implements ILinkConfig
{
	protected Class<?> left, right;

	public IndependentLinkConfig(String alias)
	{
		ParamChecker.assertParamNotNullOrEmpty(alias, "alias");
		this.alias = alias;
	}

	public Class<?> getLeft()
	{
		return left;
	}

	public void setLeft(Class<?> left)
	{
		this.left = left;
	}

	public Class<?> getRight()
	{
		return right;
	}

	public void setRight(Class<?> right)
	{
		this.right = right;
	}
}
