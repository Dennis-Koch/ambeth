package de.osthus.ambeth.util.setup;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.util.ReflectUtil;

public class DataSetup implements IDataSetup, IDatasetBuilderExtendable
{
	@LogInstance
	private ILogger log;

	protected final IExtendableContainer<IDatasetBuilder> datasetBuilderContainer = new DefaultExtendableContainer<IDatasetBuilder>(IDatasetBuilder.class,
			"TestBedBuilders");

	@Override
	public void registerDatasetBuilder(IDatasetBuilder testBedBuilder)
	{
		datasetBuilderContainer.register(testBedBuilder);
	}

	@Override
	public void unregisterDatasetBuilder(IDatasetBuilder testBedBuilder)
	{
		datasetBuilderContainer.unregister(testBedBuilder);
		eraseEntityReference(testBedBuilder);
	}

	@Override
	public IDataSetupWithAuthorization resolveDataSetupWithAuthorization()
	{
		for (IDatasetBuilder datasetBuilder : datasetBuilderContainer.getExtensions())
		{
			if (datasetBuilder instanceof IDataSetupWithAuthorization)
			{
				return (IDataSetupWithAuthorization) datasetBuilder;
			}
		}
		return null;
	}

	@Override
	public Collection<Object> executeDatasetBuilders()
	{
		IdentityHashSet<Object> initialDataset = new IdentityHashSet<Object>();
		List<IDatasetBuilder> sortedBuilders = determineExecutionOrder(datasetBuilderContainer);
		for (IDatasetBuilder datasetBuilder : sortedBuilders)
		{
			Collection<Object> dataset = datasetBuilder.buildDataset();
			if (dataset != null)
			{
				initialDataset.addAll(dataset);
			}
		}
		return initialDataset;
	}

	private List<IDatasetBuilder> determineExecutionOrder(IExtendableContainer<IDatasetBuilder> datasetBuilderContainer)
	{
		List<IDatasetBuilder> sortedBuilders = new ArrayList<IDatasetBuilder>();
		Collection<Class<? extends IDatasetBuilder>> processedBuilders = new HashSet<Class<? extends IDatasetBuilder>>();

		IDatasetBuilder[] datasetBuilders = datasetBuilderContainer.getExtensions();
		outer: while (processedBuilders.size() < datasetBuilders.length)
		{
			for (IDatasetBuilder datasetBuilder : datasetBuilders)
			{
				if (!processedBuilders.contains(datasetBuilder.getClass())
						&& (datasetBuilder.getDependsOn() == null || processedBuilders.containsAll(datasetBuilder.getDependsOn())))
				{
					processedBuilders.add(datasetBuilder.getClass());
					sortedBuilders.add(datasetBuilder);
					continue outer;
				}
			}
			log.error("All Dataset Builders: " + Arrays.asList(datasetBuilders));
			log.error("Dataset Builders: " + processedBuilders);
			throw new RuntimeException("Unable to fullfil DatasetBuilder dependencies!");
		}

		return sortedBuilders;
	}

	@Override
	public void eraseEntityReferences()
	{
		IDatasetBuilder[] extensions = datasetBuilderContainer.getExtensions();
		for (IDatasetBuilder extension : extensions)
		{
			eraseEntityReference(extension);
		}
	}

	protected void eraseEntityReference(IDatasetBuilder datasetBuilder)
	{
		for (Field field : ReflectUtil.getDeclaredFields(datasetBuilder.getClass()))
		{
			try
			{
				if (Modifier.isStatic(field.getModifiers()))
				{
					if (isFieldValueToBeErased(field.get(null)))
					{
						field.set(null, null);
					}
				}
				else if (isFieldValueToBeErased(field.get(datasetBuilder)))
				{
					field.set(datasetBuilder, null);
				}
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	protected boolean isFieldValueToBeErased(Object value)
	{
		if (value instanceof IObjRefContainer)
		{
			return true;
		}
		if (value instanceof Collection<?>)
		{
			Iterator<?> iter = ((Collection<?>) value).iterator();
			while (iter.hasNext())
			{
				if (isFieldValueToBeErased(iter.next()))
				{
					iter.remove();
				}
			}
		}
		return false;
	}
}
