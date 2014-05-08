package de.osthus.ambeth.query;

public interface IQueryCreator
{
	<T> IQuery<T> createCustomQuery(IQueryBuilder<T> qb);
}
