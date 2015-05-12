package de.osthus.ambeth.util.setup;

import java.util.Collection;

public interface IDataSetup
{
	void eraseEntityReferences();

	IDataSetupWithAuthorization resolveDataSetupWithAuthorization();

	Collection<Object> executeDatasetBuilders();
}
