package de.osthus.ambeth.util.setup;

public interface IDatasetBuilderExtensionExtendable
{
	void registerTestBedBuilderExtension(IDatasetBuilder testBedBuilder);

	void unregisterTestBedBuilderExtension(IDatasetBuilder testBedBuilder);
}
