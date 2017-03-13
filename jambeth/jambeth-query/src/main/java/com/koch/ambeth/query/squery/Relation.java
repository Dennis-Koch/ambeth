package com.koch.ambeth.query.squery;

public enum Relation
{
	AND, OR;

	public static Relation build(String relation)
	{
		if (relation == null)
		{
			return AND;
		}
		else
		{
			return Relation.valueOf(relation.toUpperCase());
		}
	}
}
