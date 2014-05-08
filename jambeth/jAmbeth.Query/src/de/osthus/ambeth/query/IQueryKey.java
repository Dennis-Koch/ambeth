package de.osthus.ambeth.query;

public interface IQueryKey
{
	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();
}
