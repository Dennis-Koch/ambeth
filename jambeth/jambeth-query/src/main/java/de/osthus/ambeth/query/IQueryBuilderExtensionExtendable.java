package de.osthus.ambeth.query;

public interface IQueryBuilderExtensionExtendable
{
	void registerQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension);

	void unregisterQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension);
}
