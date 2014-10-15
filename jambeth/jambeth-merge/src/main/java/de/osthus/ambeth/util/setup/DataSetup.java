package de.osthus.ambeth.util.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.PersistenceException;

import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class DataSetup implements IDataSetup, IDatasetBuilderExtensionExtendable
{
	@LogInstance
	private ILogger log;

	protected final IExtendableContainer<IDatasetBuilder> datasetBuilderContainer = new DefaultExtendableContainer<IDatasetBuilder>(IDatasetBuilder.class,
			"TestBedBuilders");

	@Override
	public void registerTestBedBuilderExtension(IDatasetBuilder testBedBuilder)
	{
		datasetBuilderContainer.register(testBedBuilder);
	}

	@Override
	public void unregisterTestBedBuilderExtension(IDatasetBuilder testBedBuilder)
	{
		datasetBuilderContainer.unregister(testBedBuilder);
	}

	@Override
	public Collection<Object> executeDatasetBuilders()
	{
		IdentityHashSet<Object> initialDataset = new IdentityHashSet<Object>();
		List<IDatasetBuilder> sortedBuilders = determineExecutionOrder(datasetBuilderContainer);
		for (IDatasetBuilder datasetBuilder : sortedBuilders)
		{
			datasetBuilder.buildDataset(initialDataset);
		}
		return initialDataset;
	}

	private List<IDatasetBuilder> determineExecutionOrder(IExtendableContainer<IDatasetBuilder> datasetBuilderContainer)
	{
		List<IDatasetBuilder> sortedBuilders = new ArrayList<IDatasetBuilder>();
		Collection<Class<? extends IDatasetBuilder>> processedBuilders = new HashSet<Class<? extends IDatasetBuilder>>();

		IDatasetBuilder[] datasetBuilders = datasetBuilderContainer.getExtensions();
		boolean dependencyFound;
		while (processedBuilders.size() < datasetBuilders.length)
		{
			dependencyFound = false;
			for (IDatasetBuilder datasetBuilder : datasetBuilders)
			{
				if (!processedBuilders.contains(datasetBuilder.getClass())
						&& (datasetBuilder.getDependsOn() == null || processedBuilders.containsAll(datasetBuilder.getDependsOn())))
				{
					processedBuilders.add(datasetBuilder.getClass());
					sortedBuilders.add(datasetBuilder);
					dependencyFound = true;
					break;
				}
			}
			if (!dependencyFound)
			{
				log.error("All Dataset Builders: " + Arrays.asList(datasetBuilders));
				log.error("Dataset Builders: " + processedBuilders);
				throw new PersistenceException("Unable to fullfil DatasetBuilder dependencies!");
			}
		}

		return sortedBuilders;

	}
}
