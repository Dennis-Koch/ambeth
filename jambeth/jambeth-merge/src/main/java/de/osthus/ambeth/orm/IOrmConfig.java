package de.osthus.ambeth.orm;

public interface IOrmConfig
{
	String getName();

	boolean isExplicitlyNotMergeRelevant();
}
