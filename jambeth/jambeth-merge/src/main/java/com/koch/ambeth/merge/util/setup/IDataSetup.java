package com.koch.ambeth.merge.util.setup;

import java.util.Collection;

public interface IDataSetup
{
	void eraseEntityReferences();

	void refreshEntityReferences();

	IDataSetupWithAuthorization resolveDataSetupWithAuthorization();

	Collection<Object> executeDatasetBuilders();
}
