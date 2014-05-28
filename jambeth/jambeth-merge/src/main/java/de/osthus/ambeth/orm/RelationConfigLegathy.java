package de.osthus.ambeth.orm;

import de.osthus.ambeth.util.ParamChecker;

public class RelationConfigLegathy implements IRelationConfig
{
	private final String name;

	private boolean explicitlyNotMergeRelevant;

	private final boolean toOne;

	private Class<?> linkedEntityType;

	private boolean doDelete;

	private boolean mayDelete;

	private String constraintName;

	private String joinTableName;

	private String fromFieldName;

	private String toFieldName;

	private String toAttributeName;

	public RelationConfigLegathy(String name, boolean toOne)
	{
		ParamChecker.assertParamNotNullOrEmpty(name, "name");

		this.name = name;
		this.toOne = toOne;
	}

	@Override
	public String getName()
	{
		return name;
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

	public boolean isToOne()
	{
		return toOne;
	}

	public Class<?> getLinkedEntityType()
	{
		return linkedEntityType;
	}

	public void setLinkedEntityType(Class<?> linkedEntityType)
	{
		this.linkedEntityType = linkedEntityType;
	}

	public boolean doDelete()
	{
		return doDelete;
	}

	public void setDoDelete(boolean doDelete)
	{
		this.doDelete = doDelete;
	}

	public boolean mayDelete()
	{
		return mayDelete;
	}

	public void setMayDelete(boolean mayDelete)
	{
		this.mayDelete = mayDelete;
	}

	public String getConstraintName()
	{
		return constraintName;
	}

	public void setConstraintName(String constraintName)
	{
		this.constraintName = constraintName;
	}

	public String getJoinTableName()
	{
		return joinTableName;
	}

	public void setJoinTableName(String joinTableName)
	{
		this.joinTableName = joinTableName;
	}

	public String getFromFieldName()
	{
		return fromFieldName;
	}

	public String getToFieldName()
	{
		return toFieldName;
	}

	public void setFromFieldName(String fromFieldName)
	{
		this.fromFieldName = fromFieldName;
	}

	public void setToFieldName(String toFieldName)
	{
		this.toFieldName = toFieldName;
	}

	public String getToAttributeName()
	{
		return toAttributeName;
	}

	public void setToAttributeName(String toAttributeName)
	{
		this.toAttributeName = toAttributeName;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof RelationConfigLegathy)
		{
			RelationConfigLegathy other = (RelationConfigLegathy) obj;
			return name.equals(other.getName());
		}
		else
		{
			return false;
		}
	}
}
