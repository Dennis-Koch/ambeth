package com.koch.ambeth.merge.util.setup;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.extendable.IExtendableContainer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.security.ILightweightSecurityContext;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

public class DataSetup implements IDataSetup, IDatasetBuilderExtendable {
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected ILightweightSecurityContext securityContext;

	protected final IExtendableContainer<IDatasetBuilder> datasetBuilderContainer =
			new DefaultExtendableContainer<>(IDatasetBuilder.class, "TestBedBuilders");

	@Override
	public void registerDatasetBuilder(IDatasetBuilder testBedBuilder) {
		datasetBuilderContainer.register(testBedBuilder);
	}

	@Override
	public void unregisterDatasetBuilder(IDatasetBuilder testBedBuilder) {
		datasetBuilderContainer.unregister(testBedBuilder);
		eraseEntityReference(testBedBuilder);
	}

	@Override
	public IDataSetupWithAuthorization resolveDataSetupWithAuthorization() {
		for (IDatasetBuilder datasetBuilder : datasetBuilderContainer.getExtensions()) {
			if (datasetBuilder instanceof IDataSetupWithAuthorization) {
				return (IDataSetupWithAuthorization) datasetBuilder;
			}
		}
		return null;
	}

	@Override
	public Collection<Object> executeDatasetBuilders() {
		IdentityHashSet<Object> initialDataset = new IdentityHashSet<>();
		List<IDatasetBuilder> sortedBuilders = determineExecutionOrder(datasetBuilderContainer);
		for (IDatasetBuilder datasetBuilder : sortedBuilders) {
			Collection<Object> dataset = datasetBuilder.buildDataset();
			if (dataset != null) {
				initialDataset.addAll(dataset);
			}
		}
		return initialDataset;
	}

	private List<IDatasetBuilder> determineExecutionOrder(
			IExtendableContainer<IDatasetBuilder> datasetBuilderContainer) {
		List<IDatasetBuilder> sortedBuilders = new ArrayList<>();
		Collection<Class<? extends IDatasetBuilder>> processedBuilders = new HashSet<>();

		IDatasetBuilder[] datasetBuilders = datasetBuilderContainer.getExtensions();
		outer: while (processedBuilders.size() < datasetBuilders.length) {
			for (IDatasetBuilder datasetBuilder : datasetBuilders) {
				if (!processedBuilders.contains(datasetBuilder.getClass())
						&& (datasetBuilder.getDependsOn() == null
								|| processedBuilders.containsAll(datasetBuilder.getDependsOn()))) {
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
	public void eraseEntityReferences() {
		IDatasetBuilder[] extensions = datasetBuilderContainer.getExtensions();
		for (IDatasetBuilder extension : extensions) {
			eraseEntityReference(extension);
		}
	}

	protected void eraseEntityReference(IDatasetBuilder datasetBuilder) {
		for (Field field : ReflectUtil.getDeclaredFields(datasetBuilder.getClass())) {
			try {
				if (Modifier.isStatic(field.getModifiers())) {
					if (eraseFieldValueIfNecessary(field.get(null))) {
						field.set(null, null);
					}
				}
				else if (eraseFieldValueIfNecessary(field.get(datasetBuilder))) {
					field.set(datasetBuilder, null);
				}
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	protected boolean eraseFieldValueIfNecessary(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof IObjRefContainer) {
			return true;
		}
		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			for (int a = 0, size = length; a < size; a++) {
				if (eraseFieldValueIfNecessary(Array.get(value, a))) {
					Array.set(value, a, null);
				}
			}
		}
		else if (value instanceof IMap) {
			Iterator<? extends Entry<?, ?>> iter = ((IMap<?, ?>) value).iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = iter.next();
				Object key = entry.getKey();
				Object entryValue = entry.getValue();
				if (eraseFieldValueIfNecessary(key) || eraseFieldValueIfNecessary(entryValue)) {
					iter.remove();
					continue;
				}
			}
		}
		else if (value instanceof Map) {
			Iterator<? extends Entry<?, ?>> iter = ((Map<?, ?>) value).entrySet().iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = iter.next();
				Object key = entry.getKey();
				Object entryValue = entry.getValue();
				if (eraseFieldValueIfNecessary(key) || eraseFieldValueIfNecessary(entryValue)) {
					iter.remove();
					continue;
				}
			}
		}
		else if (value instanceof Collection<?>) {
			Iterator<?> iter = ((Collection<?>) value).iterator();
			while (iter.hasNext()) {
				if (eraseFieldValueIfNecessary(iter.next())) {
					iter.remove();
				}
			}
		}
		return false;
	}

	@Override
	public void refreshEntityReferences() {
		IDatasetBuilder[] extensions = datasetBuilderContainer.getExtensions();
		ArrayList<IObjRef> objRefs = new ArrayList<>();
		ArrayList<IBackgroundWorkerDelegate> runnables = new ArrayList<>();
		IdentityHashMap<IObjRef, Object> objRefToEntityMap = new IdentityHashMap<>();
		boolean isAuthenticated = securityContext.isAuthenticated();
		IdentityHashSet<ICache> cachesToClear = new IdentityHashSet<>();
		for (IDatasetBuilder extension : extensions) {
			refreshEntityReference(extension, objRefs, runnables, objRefToEntityMap, isAuthenticated,
					cachesToClear);
		}
		for (ICache cache : cachesToClear) {
			((IWritableCache) cache).clear();
		}
		IList<Object> objects = null;
		if (!objRefs.isEmpty()) {
			objects = cache.getObjects(objRefs, CacheDirective.returnMisses());
			for (int a = objRefs.size(); a-- > 0;) {
				Object entity = objects.get(a);
				if (entity == null) {
					continue;
				}
				objRefToEntityMap.put(objRefs.get(a), entity);
			}
		}
		while (!runnables.isEmpty()) {
			IBackgroundWorkerDelegate[] runnablesArray =
					runnables.toArray(IBackgroundWorkerDelegate.class);
			runnables.clear();
			for (IBackgroundWorkerDelegate runnable : runnablesArray) {
				try {
					runnable.invoke();
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
	}

	protected void refreshEntityReference(final IDatasetBuilder datasetBuilder,
			IList<IObjRef> objRefs, IList<IBackgroundWorkerDelegate> runnables,
			final IMap<IObjRef, Object> objRefToEntityMap, boolean isAuthenticated,
			ISet<ICache> cachesToClear) {
		for (final Field field : ReflectUtil.getDeclaredFields(datasetBuilder.getClass())) {
			try {
				if (Modifier.isStatic(field.getModifiers())) {
					final IObjRef objRef = refreshFieldValue(field.get(null), objRefs, runnables,
							objRefToEntityMap, isAuthenticated, cachesToClear);
					if (objRef == null) {
						continue;
					}
					objRefs.add(objRef);
					runnables.add(new IBackgroundWorkerDelegate() {
						@Override
						public void invoke() throws Throwable {
							field.set(null, objRefToEntityMap.get(objRef));
						}
					});
					continue;
				}
				final IObjRef objRef = refreshFieldValue(field.get(datasetBuilder), objRefs, runnables,
						objRefToEntityMap, isAuthenticated, cachesToClear);
				if (objRef == null) {
					continue;
				}
				objRefs.add(objRef);
				runnables.add(new IBackgroundWorkerDelegate() {
					@Override
					public void invoke() throws Throwable {
						field.set(datasetBuilder, objRefToEntityMap.get(objRef));
					}
				});
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	protected IObjRef refreshFieldValue(final Object value, IList<IObjRef> objRefs,
			IList<IBackgroundWorkerDelegate> runnables, final IMap<IObjRef, Object> objRefToEntityMap,
			boolean isAuthenticated, ISet<ICache> cachesToClear) {
		if (value == null) {
			return null;
		}
		if (value instanceof IObjRefContainer) {
			if (!isAuthenticated) {
				ICache cache = ((IObjRefContainer) value).get__Cache();
				if (cache != null) {
					cachesToClear.add(cache.getCurrentCache());
				}
				((IObjRefContainer) value).detach();

				return null;
			}
			return objRefHelper.entityToObjRef(value);
		}
		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			for (int a = 0, size = length; a < size; a++) {
				final IObjRef objRef = refreshFieldValue(Array.get(value, a), objRefs, runnables,
						objRefToEntityMap, isAuthenticated, cachesToClear);
				if (objRef == null) {
					continue;
				}
				objRefs.add(objRef);
				final int index = a;
				runnables.add(new IBackgroundWorkerDelegate() {
					@Override
					public void invoke() throws Throwable {
						Object entity = objRefToEntityMap.get(objRef);
						Array.set(value, index, entity);
					}
				});
			}
		}
		else if (value instanceof Collection<?>) {
			Object[] array = ((Collection<?>) value).toArray();
			for (final Object item : array) {
				final IObjRef objRef = refreshFieldValue(item, objRefs, runnables, objRefToEntityMap,
						isAuthenticated, cachesToClear);
				if (objRef == null) {
					continue;
				}
				objRefs.add(objRef);
				runnables.add(new IBackgroundWorkerDelegate() {

					@Override
					public void invoke() throws Throwable {
						Object entity = objRefToEntityMap.get(objRef);
						if (value instanceof Set<?>) {
							((Set<Object>) value).remove(item);
							if (entity != null) {
								((Set<Object>) value).add(entity);
							}
						}
						else {
							((List<Object>) value).remove(item);
							if (entity != null) {
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
