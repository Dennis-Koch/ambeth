package de.osthus.ambeth.util.setup;

import java.util.Collection;

public interface IDatasetBuilder
{
	Collection<Object> buildDataset();

	Collection<Class<? extends IDatasetBuilder>> getDependsOn();
}
