package de.osthus.ambeth.util.setup;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ReflectUtil;

public class DataSetup implements IDataSetup, IDatasetBuilderExtendable
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IObjRefHelper objRefHelper;

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
					if (eraseFieldValueIfNecessary(field.get(null)))
					{
						field.set(null, null);
					}
				}
				else if (eraseFieldValueIfNecessary(field.get(datasetBuilder)))
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

	protected boolean eraseFieldValueIfNecessary(Object value)
	{
		if (value == null)
		{
			return false;
		}
		if (value instanceof IObjRefContainer)
		{
			return true;
		}
		if (value.getClass().isArray())
		{
			int length = Array.getLength(value);
			for (int a = 0, size = length; a < size; a++)
			{
				if (eraseFieldValueIfNecessary(Array.get(value, a)))
				{
					Array.set(value, a, null);
				}
			}
		}
		else if (value instanceof Collection<?>)
		{
			Iterator<?> iter = ((Collection<?>) value).iterator();
			while (iter.hasNext())
			{
				if (eraseFieldValueIfNecessary(iter.next()))
				{
					iter.remove();
				}
			}
		}
		return false;
	}

	@Override
	public void refreshEntityReferences()
	{
		IDatasetBuilder[] extensions = datasetBuilderContainer.getExtensions();
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
		ArrayList<IBackgroundWorkerDelegate> runnables = new ArrayList<IBackgroundWorkerDelegate>();
		IdentityHashMap<IObjRef, Object> objRefToEntityMap = new IdentityHashMap<IObjRef, Object>();
		for (IDatasetBuilder extension : extensions)
		{
			refreshEntityReference(extension, objRefs, runnables, objRefToEntityMap);
		}
		IList<Object> objects = null;
		if (objRefs.size() > 0)
		{
			objects = cache.getObjects(objRefs, CacheDirective.returnMisses());
			for (int a = objRefs.size(); a-- > 0;)
			{
				Object entity = objects.get(a);
				if (entity == null)
				{
					continue;
				}
				objRefToEntityMap.put(objRefs.get(a), entity);
			}
		}
		while (runnables.size() > 0)
		{
			IBackgroundWorkerDelegate[] runnablesArray = runnables.toArray(IBackgroundWorkerDelegate.class);
			runnables.clear();
			for (IBackgroundWorkerDelegate runnable : runnablesArray)
			{
				try
				{
					runnable.invoke();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
	}

	protected void refreshEntityReference(final IDatasetBuilder datasetBuilder, IList<IObjRef> objRefs, IList<IBackgroundWorkerDelegate> runnables,
			final IMap<IObjRef, Object> objRefToEntityMap)
	{
		for (final Field field : ReflectUtil.getDeclaredFields(datasetBuilder.getClass()))
		{
			try
			{
				if (Modifier.isStatic(field.getModifiers()))
				{
					final IObjRef objRef = refreshFieldValue(field.get(null), objRefs, runnables, objRefToEntityMap);
					if (objRef == null)
					{
						continue;
					}
					objRefs.add(objRef);
					runnables.add(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							field.set(null, objRefToEntityMap.get(objRef));
						}
					});
					continue;
				}
				final IObjRef objRef = refreshFieldValue(field.get(datasetBuilder), objRefs, runnables, objRefToEntityMap);
				if (objRef == null)
				{
					continue;
				}
				objRefs.add(objRef);
				runnables.add(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						field.set(datasetBuilder, objRefToEntityMap.get(objRef));
					}
				});
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	protected IObjRef refreshFieldValue(final Object value, IList<IObjRef> objRefs, IList<IBackgroundWorkerDelegate> runnables,
			final IMap<IObjRef, Object> objRefToEntityMap)
	{
		if (value == null)
		{
			return null;
		}
		if (value instanceof IObjRefContainer)
		{
			return objRefHelper.entityToObjRef(value);
		}
		if (value.getClass().isArray())
		{
			int length = Array.getLength(value);
			for (int a = 0, size = length; a < size; a++)
			{
				final IObjRef objRef = refreshFieldValue(Array.get(value, a), objRefs, runnables, objRefToEntityMap);
				if (objRef == null)
				{
					continue;
				}
				objRefs.add(objRef);
				final int index = a;
				runnables.add(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						Object entity = objRefToEntityMap.get(objRef);
						Array.set(value, index, entity);
					}
				});
			}
		}
		else if (value instanceof Collection<?>)
		{
			Object[] array = ((Collection<?>) value).toArray();
			for (final Object item : array)
			{
				final IObjRef objRef = refreshFieldValue(item, objRefs, runnables, objRefToEntityMap);
				if (objRef == null)
				{
					continue;
				}
				objRefs.add(objRef);
				runnables.add(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						Object entity = objRefToEntityMap.get(objRef);
						if (value instanceof Set<?>)
						{
							((Set<Object>) value).remove(item);
							if (entity != null)
							{
								((Set<Object>) value).add(entity);
							}
						}
						else
						{
							((List<Object>) value).remove(item);
							if (entity != null)
							{
								((List<Object>) value).add(entity);
							}
						}
					}
				});
			}
		}
		return null;
	}
}
