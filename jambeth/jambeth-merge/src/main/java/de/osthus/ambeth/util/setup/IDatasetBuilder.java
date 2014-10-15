package de.osthus.ambeth.util.setup;

import java.util.Collection;

public interface IDatasetBuilder
{
	void buildDataset(Collection<Object> initialTestDataset);

	Collection<Class<? extends IDatasetBuilder>> getDependsOn();
}
