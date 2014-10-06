package de.osthus.ambeth.testutil.setup.data;

import java.util.Collection;

public interface IDatasetBuilder
{
	Collection<Object> buildDataset();

	Collection<Class<? extends IDatasetBuilder>> getDependsOn();
}
