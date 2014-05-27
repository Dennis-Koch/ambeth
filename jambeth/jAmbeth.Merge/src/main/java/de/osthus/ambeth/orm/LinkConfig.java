package de.osthus.ambeth.orm;

import de.osthus.ambeth.util.ParamChecker;

public class LinkConfig implements ILinkConfig
{
	protected String source;

	protected String alias;

	protected CascadeDeleteDirection cascadeDeleteDirection = CascadeDeleteDirection.NONE;

	public LinkConfig(String source)
	{
		ParamChecker.assertParamNotNull(source, "source");
		this.source = source;
	}

	protected LinkConfig()
	{
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	@Override
	public CascadeDeleteDirection getCascadeDeleteDirection()
	{
		return cascadeDeleteDirection;
	}

	public void setCascadeDeleteDirection(CascadeDeleteDirection cascadeDeleteDirection)
	{
		this.cascadeDeleteDirection = cascadeDeleteDirection;
	}
}
