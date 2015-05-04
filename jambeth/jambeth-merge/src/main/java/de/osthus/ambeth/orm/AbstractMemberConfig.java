package de.osthus.ambeth.orm;

import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractMemberConfig implements IMemberConfig
{
	private final String name;

	private String definedBy;

	private boolean alternateId;

	private boolean ignore;

	private boolean isTransient;

	private boolean explicitlyNotMergeRelevant;

	public AbstractMemberConfig(String name)
	{
		ParamChecker.assertParamNotNullOrEmpty(name, "name");
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isAlternateId()
	{
		return alternateId;
	}

	public void setAlternateId(boolean alternateId)
	{
		this.alternateId = alternateId;
	}

	@Override
	public boolean isTransient()
	{
		return isTransient;
	}

	public void setTransient(boolean isTransient)
	{
		this.isTransient = isTransient;
	}

	@Override
	public String getDefinedBy()
	{
		return definedBy;
	}

	public void setDefinedBy(String definedBy)
	{
		this.definedBy = definedBy;
	}

	@Override
	public boolean isIgnore()
	{
		return ignore;
	}

	public void setIgnore(boolean ignore)
	{
		this.ignore = ignore;
	}

	@Override
	public boolean isExplicitlyNotMergeRelevant()
	{
		return explicitlyNotMergeRelevant;
	}

	public void setExplicitlyNotMergeRelevant(boolean explicitlyNotMergeRelevant)
	{
		this.explicitlyNotMergeRelevant = explicitlyNotMergeRelevant;
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}

	@Override
	public abstract boolean equals(Object obj);

	public boolean equals(AbstractMemberConfig other)
	{
		return getName().equals(other.getName());
	}
}