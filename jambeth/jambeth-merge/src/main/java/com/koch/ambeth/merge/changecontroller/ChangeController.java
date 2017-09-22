package com.koch.ambeth.merge.changecontroller;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ClassExtendableListContainer;
import com.koch.ambeth.merge.IMergeController;
import com.koch.ambeth.merge.IMergeListener;
import com.koch.ambeth.merge.MergeHandle;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.incremental.IMergePipelineFinishListener;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;

/**
 * A ChangeController listens on all changes that should be persisted by implementing a
 * {@link IMergeListener}.
 *
 * To use this controller, you have to link it with the
 * {@link com.koch.ambeth.merge.IMergeExtendable} interface.
 */
public class ChangeController
		implements IChangeController, IChangeControllerExtendable, IMergeListener {
	public class MergeProcessFinishListener implements IMergePipelineFinishListener {
		@Override
		public void mergePipelineFinished(boolean success,
				IIncrementalMergeState incrementalMergeState) {
			ISet<IMergePipelineListener> pipelineExtensions =
					incrementalMergeState.getCustomState(key);
			for (IMergePipelineListener pipelineExtension : pipelineExtensions) {
				if (success) {
					pipelineExtension.flushPipeline(incrementalMergeState);
				}
				else {
					pipelineExtension.rollbackPipeline(incrementalMergeState);
				}
			}
		}
	}

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	protected ThreadLocal<Boolean> edblActiveTL = new ThreadLocal<>();

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IMergeController mergeController;

	@Property(name = MergeConfigurationConstants.edblActive, defaultValue = "true")
	protected boolean edblActive;

	private final String key = getClass().getSimpleName() + "_PIPELINE_LISTENERS";

	private final String cleanupKey = getClass().getSimpleName() + "_PIPELINE_CLEANUP";

	protected final ClassExtendableListContainer<IChangeControllerExtension<?>> extensions =
			new ClassExtendableListContainer<>(
					"change controller extension", "entity");

	protected final SmartCopyMap<Class<?>, IChangeControllerExtension<?>[]> typeToSortedExtensions =
			new SmartCopyMap<>();

	@Override
	public ICUDResult preMerge(ICUDResult cudResult, IIncrementalMergeState incrementalMergeState) {
		if (!edblActive || (edblActiveTL.get() != null && Boolean.FALSE.equals(edblActiveTL.get()))) {
			return cudResult;
		}
		List<IChangeContainer> changes = cudResult.getAllChanges();
		if (!changes.isEmpty() && !extensions.isEmpty()) {
			// used to lookup the previous values
			IDisposableCache oldCache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false,
					Boolean.FALSE, "ChangeController.PreMerge");
			try {
				IObjRef[] references = extractReferences(changes);
				List<Object> newObjects = cudResult.getOriginalRefs();
				List<Object> oldObjects = oldCache.getObjects(references, CacheDirective.returnMisses());

				ICache cache = incrementalMergeState.getStateCache();
				boolean extensionCalled;
				IStateRollback rollback = cacheContext.pushCache(cache);
				try {
					extensionCalled = processChanges(newObjects, oldObjects, changes, incrementalMergeState);
				}
				finally {
					rollback.rollback();
				}
				// If no extension has been called, we have no changes and do not need to change the
				// CudResult
				if (extensionCalled) {
					// Load all new objects from Cache (maybe there have been some created)
					Collection<Object> objectsToMerge = retrieveChangedObjects(newObjects, cache);
					// A merge handler that contains a reference to the old cache is needed ...
					MergeHandle mergeHandle = beanContext.registerBean(MergeHandle.class)
							.propertyValue("Cache", oldCache).propertyValue("PrivilegedCache", oldCache).finish();
					// ... to create a new CudResult via the mergeController
					cudResult = mergeController.mergeDeep(objectsToMerge, mergeHandle);
				}
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			finally {
				oldCache.dispose();
			}
		}
		return cudResult;
	}

	protected Collection<Object> retrieveChangedObjects(Collection<Object> objectsBefore,
			ICache cache) {
		// We have at least as many objects as before
		final Set<Object> newObjects = new HashSet<>(objectsBefore);
		HandleContentDelegate delegate = new HandleContentDelegate() {
			@Override
			public void invoke(Class<?> entityType, byte idIndex, Object id, Object value) {
				newObjects.add(value);
			}
		};
		cache.getContent(delegate);
		return newObjects;
	}

	/**
	 * Iterate over all entities and process each pair of new and old values.
	 *
	 * @param newEntities
	 * @param oldEntities
	 * @param changes
	 * @return true if any of the extensions have been called
	 */
	@SuppressWarnings("rawtypes")
	protected boolean processChanges(List<Object> newEntities, List<Object> oldEntities,
			List<IChangeContainer> changes, IIncrementalMergeState incrementalMergeState) {
		int size = newEntities.size();
		ParamChecker.assertTrue(size == oldEntities.size(),
				"number of old and new objects should be equal");
		CacheView views = new CacheView(newEntities, oldEntities, changes, incrementalMergeState);
		IdentityLinkedSet<IChangeControllerExtension<?>> calledExtensionsSet =
				new IdentityLinkedSet<>();

		try {
			for (int index = 0; index < size; index++) {
				Object newEntity = newEntities.get(index);
				boolean toBeDeleted = ((IDataObject) newEntity).isToBeDeleted();
				boolean toBeCreated = false;
				Object oldEntity = oldEntities.get(index);
				if (oldEntity == null) {
					toBeCreated = true;
				}
				processChange(newEntity, oldEntity, toBeDeleted, toBeCreated, incrementalMergeState, views,
						calledExtensionsSet);
			}
			views.processRunnables();
			for (IChangeControllerExtension ext : calledExtensionsSet) {
				if (ext instanceof IMergeStepListener) {
					((IMergeStepListener) ext).flushStep(views);
				}
			}
			boolean result = !calledExtensionsSet.isEmpty();
			Object customState = incrementalMergeState.getCustomState(key);
			if (customState != null) {
				// register pipeline cleanup
				if (incrementalMergeState.getCustomState(cleanupKey) == null) {
					incrementalMergeState
							.registerMergeProcessFinishListener(new MergeProcessFinishListener());
					incrementalMergeState.setCustomState(cleanupKey, Boolean.TRUE);
				}
			}
			return result;
		}
		catch (Throwable e) {
			for (IChangeControllerExtension ext : calledExtensionsSet) {
				if (ext instanceof IMergeStepListener) {
					((IMergeStepListener) ext).rollbackStep(views);
				}
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * Process a single change represented by the new and old version of the object. We look up if
	 * there are any extensions registered for the given objects. If yes, the extensions are called
	 * with the change.
	 *
	 * @param newEntity the new version of the entity
	 * @param oldEntity the old version of the entity
	 * @param toBeDeleted true, if the new entity is to be deleted
	 * @param toBeCreated true, if the new entity is to be created
	 * @param views
	 * @param calledExtensionsSet
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void processChange(Object newEntity, Object oldEntity, boolean toBeDeleted,
			boolean toBeCreated, IIncrementalMergeState incrementalMergeState, ICacheView views,
			ISet<IChangeControllerExtension<?>> calledExtensionsSet) {
		// Both objects should be of the same class, so we just need one them. We just have to keep in
		// mind that one of them could be null.
		Class<?> entityType = newEntity != null ? newEntity.getClass() : oldEntity.getClass();
		// Search for registered extensions for the implemented classes
		IChangeControllerExtension<?>[] sortedExtensions = typeToSortedExtensions.get(entityType);
		if (sortedExtensions == null) {
			sortedExtensions = extensions.getExtensions(entityType)
					.toArray(IChangeControllerExtension.class);
			Arrays.sort(sortedExtensions);
			typeToSortedExtensions.put(entityType, sortedExtensions);
		}
		ISet<IMergePipelineListener> calledPipelineExtensionsSet = null;
		for (IChangeControllerExtension ext : sortedExtensions) {
			if (ext instanceof IMergePipelineListener) {
				if (calledPipelineExtensionsSet == null) {
					calledPipelineExtensionsSet = views.getCustomState(key);
					if (calledPipelineExtensionsSet == null) {
						calledPipelineExtensionsSet = new IdentityLinkedSet<>();
						views.setCustomState(key, calledPipelineExtensionsSet);
					}
				}
				if (calledPipelineExtensionsSet.add((IMergePipelineListener) ext)) {
					((IMergePipelineListener) ext).queuePipeline(incrementalMergeState);
				}
			}
			if (calledExtensionsSet.add(ext) && ext instanceof IMergeStepListener) {
				((IMergeStepListener) ext).queueStep(views);
			}
			if (calledExtensionsSet.add(ext) && ext instanceof IMergeStepListener) {
				((IMergeStepListener) ext).queueStep(views);
			}
			ext.processChange(newEntity, oldEntity, toBeDeleted, toBeCreated, views);

		}
	}

	/**
	 * Create an array that contains all references of the given changes.
	 *
	 * @param changes
	 * @return the created array, never <code>null</code>
	 */
	protected IObjRef[] extractReferences(List<IChangeContainer> changes) {
		IObjRef[] references = new IObjRef[changes.size()];
		for (int a = changes.size(); a-- > 0;) {
			IChangeContainer change = changes.get(a);
			if (change instanceof CreateContainer) {
				continue;
			}
			references[a] = change.getReference();
		}
		return references;
	}

	@Override
	public void postMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs,
			IIncrementalMergeState incrementalMergeState) {
		// intentionally left blank
	}

	@Override
	public void registerChangeControllerExtension(IChangeControllerExtension<?> extension,
			Class<?> clazz) {
		ParamChecker.assertTrue(clazz.isInterface(),
				"Currently only interfaces are supported for ChangeControllerExtensions");
		extensions.register(extension, clazz);
		typeToSortedExtensions.clear();
	}

	@Override
	public void unregisterChangeControllerExtension(IChangeControllerExtension<?> extension,
			Class<?> clazz) {
		extensions.unregister(extension, clazz);
		typeToSortedExtensions.clear();
	}

	@Override
	public IStateRollback pushRunWithoutEDBL(IStateRollback... rollbacks) {
		final Boolean oldValue = edblActiveTL.get();
		edblActiveTL.set(Boolean.FALSE);
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				edblActiveTL.set(oldValue);
			}
		};
	}
}
