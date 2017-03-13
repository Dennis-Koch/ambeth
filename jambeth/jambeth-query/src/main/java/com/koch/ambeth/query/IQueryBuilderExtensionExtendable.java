package com.koch.ambeth.query;

public interface IQueryBuilderExtensionExtendable
{
	void registerQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension);

	void unregisterQueryBuilderExtension(IQueryBuilderExtension queryBuilderExtension);
}
