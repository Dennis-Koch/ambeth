package de.osthus.ambeth.query;

public interface IQueryBuilderFactory
{
	<T> IQueryBuilder<T> create(Class<T> entityType);
}
