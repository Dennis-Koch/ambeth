package de.osthus.ambeth.util.setup;

public interface IDatasetBuilderExtendable
{
	void registerDatasetBuilder(IDatasetBuilder datasetBuilder);

	void unregisterDatasetBuilder(IDatasetBuilder datasetBuilder);
}
