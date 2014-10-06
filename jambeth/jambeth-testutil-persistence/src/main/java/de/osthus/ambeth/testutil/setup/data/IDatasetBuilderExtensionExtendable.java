package de.osthus.ambeth.testutil.setup.data;

public interface IDatasetBuilderExtensionExtendable
{
	void registerTestBedBuilderExtension(IDatasetBuilder testBedBuilder);

	void unregisterTestBedBuilderExtension(IDatasetBuilder testBedBuilder);
}
