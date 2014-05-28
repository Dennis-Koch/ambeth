package de.osthus.ambeth.orm;

import de.osthus.ambeth.util.ParamChecker;

public class RelationConfig20 implements IRelationConfig
{
	private final String name;

	private final ILinkConfig link;

	private EntityIdentifier entityIdentifier;

	private boolean explicitlyNotMergeRelevant;

	public RelationConfig20(String name, ILinkConfig link)
	{
		ParamChecker.assertParamNotNullOrEmpty(name, "name");
		ParamChecker.assertParamNotNull(link, "link");

		this.name = name;
		this.link = link;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public ILinkConfig getLink()
	{
		return link;
	}

	public EntityIdentifier getEntityIdentifier()
	{
		return entityIdentifier;
	}

	public void setEntityIdentifier(EntityIdentifier entityIdentifier)
	{
		this.entityIdentifier = entityIdentifier;
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
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof RelationConfig20)
		{
			IRelationConfig other = (IRelationConfig) obj;
			return name.equals(other.getName());
		}
		else
		{
			return false;
		}
	}
}
