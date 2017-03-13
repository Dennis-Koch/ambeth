package com.koch.ambeth.merge.util.setup;

public interface IDatasetBuilderExtendable
{
	void registerDatasetBuilder(IDatasetBuilder datasetBuilder);

	void unregisterDatasetBuilder(IDatasetBuilder datasetBuilder);
}
