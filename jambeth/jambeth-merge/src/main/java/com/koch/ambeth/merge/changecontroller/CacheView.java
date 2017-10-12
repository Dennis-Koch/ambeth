package com.koch.ambeth.merge.changecontroller;

import java.time.Instant;

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.incremental.IMergePipelineFinishHook;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.ICreateOrUpdateContainer;
import com.koch.ambeth.merge.model.IUpdateItem;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class CacheView implements ICacheView {
	// objects contains all new objects
	protected final List<Object> newObjects, oldObjects;

	protected final List<IChangeContainer> changes;

	protected final IIncrementalMergeState incrementalMergeState;

	// views contains a map from an interface to all objects whose class implements it. Entries are
	// created lazily below.
	protected final HashMap<Class<?>, Collection<?>> newViews =
			new HashMap<>();
	// The entries in the oldViews lists correlate to those in the newViews list
	protected final HashMap<Class<?>, Collection<?>> oldViews =
			new HashMap<>();

	protected final Instant now = Instant.now();

	protected IdentityHashMap<Object, IChangeContainer> objectToChangeContainerMap;

	protected ArrayList<IMergeStepPreFlushHook> mergeStepPreFlushHooks;

	public CacheView(List<Object> newObjects, List<Object> oldObjects,
			List<IChangeContainer> changes, IIncrementalMergeState incrementalMergeState) {
		this.newObjects = newObjects;
		this.oldObjects = oldObjects;
		this.changes = changes;
		this.incrementalMergeState = incrementalMergeState;
	}

	@Override
	public Instant now() {
		return now;
	}

	@Override
	public IChangeContainer getChangeContainer(Object newObject) {
		if (objectToChangeContainerMap != null) {
			return objectToChangeContainerMap.get(newObject);
		}
		objectToChangeContainerMap = new IdentityHashMap<>();
		for (int a = newObjects.size(); a-- > 0;) {
			Object currNewObject = newObjects.get(a);
			IChangeContainer changeContainer = changes.get(a);
			objectToChangeContainerMap.put(currNewObject, changeContainer);
		}
		return objectToChangeContainerMap.get(newObject);
	}

	@Override
	public Set<String> getChangedMembers(Object newObject) {
		IChangeContainer changeContainer = getChangeContainer(newObject);
		if (changeContainer == null || changeContainer instanceof DeleteContainer) {
			return Collections.emptySet();
		}
		ICreateOrUpdateContainer container = (ICreateOrUpdateContainer) changeContainer;
		HashSet<String> changedMembers = new HashSet<>();
		IUpdateItem[] updateItems = container.getFullPUIs();
		if (updateItems != null) {
			for (IUpdateItem pui : updateItems) {
				if (pui != null) {
					changedMembers.add(pui.getMemberName());
				}
			}
		}
		updateItems = container.getFullRUIs();
		if (updateItems != null) {
			for (IUpdateItem rui : updateItems) {
				if (rui != null) {
					changedMembers.add(rui.getMemberName());
				}
			}
		}
		return changedMembers;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getNewObjectsOfClass(Class<T> clazz) {
		assureObjectsOfClass(clazz);
		return (Collection<T>) newViews.get(clazz);
	}

	/**
	 * Returns a list of new objects that have the given interface as type.
	 *
	 * @param clazz The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Collection<T> getOldObjectsOfClass(Class<T> clazz) {
		assureObjectsOfClass(clazz);
		return (Collection<T>) oldViews.get(clazz);
	}

	/**
	 * Make sure that all elements of the given class are computed
	 *
	 * @param clazz
	 */
	protected void assureObjectsOfClass(Class<?> clazz) {
		// We just check for the entry in newViews because newViews and oldViews are updated
		// simultaneously.
		if (!newViews.containsKey(clazz)) {
			createObjectsOfClass(clazz);
		}
	}

	protected void createObjectsOfClass(Class<?> clazz) {
		ParamChecker.assertTrue(clazz.isInterface(), clazz.getName() + " is not an interface");

		ArrayList<Object> newResult = new ArrayList<>();
		ArrayList<Object> oldResult = new ArrayList<>();
		int size = newObjects.size();
		// Filter all objects that are an instance of the given class
		for (int index = 0; index < size; index += 1) {
			Object newEntity = newObjects.get(index);
			Object oldEntity = oldObjects.get(index);
			// just get one of both that is not null (there must be at least one)
			Object anyEntity = newEntity != null ? newEntity : oldEntity;
			if (clazz.isInstance(anyEntity)) {
				newResult.add(newEntity);
				oldResult.add(oldEntity);
			}
		}
		newViews.put(clazz, newResult);
		oldViews.put(clazz, oldResult);
	}

	@Override
	public <T> T getCustomState(Object key) {
		ParamChecker.assertParamNotNull(key, "key");
		return incrementalMergeState.getCustomState(key);
	}

	@Override
	public <T> T setCustomState(Object key, Object value) {
		ParamChecker.assertParamNotNull(key, "key");
		return incrementalMergeState.setCustomState(key, value);
	}

	@Override
	public void addCustomStateItem(Object key, Object item) {
		Collection<Object> list = getCustomState(key);
		if (list == null) {
			list = new ArrayList<>();
			setCustomState(key, list);
		}
		list.add(item);
	}

	@Override
	public void queuePreFlush(IMergeStepPreFlushHook mergeStepPreFlushHook) {
		ParamChecker.assertParamNotNull(mergeStepPreFlushHook, "mergeStepPreFlushHook");
		if (mergeStepPreFlushHooks == null) {
			mergeStepPreFlushHooks = new ArrayList<>();
		}
		mergeStepPreFlushHooks.add(mergeStepPreFlushHook);
	}

	public void processRunnables() {
		if (mergeStepPreFlushHooks == null) {
			return;
		}
		// while loop because a runnable could queue cascading runnables
		while (!mergeStepPreFlushHooks.isEmpty()) {
			IMergeStepPreFlushHook[] hooks =
					mergeStepPreFlushHooks.toArray(IMergeStepPreFlushHook.class);
			mergeStepPreFlushHooks.clear();
			try {
				for (IMergeStepPreFlushHook hook : hooks) {
					hook.preFlushStep(this);
				}
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void registerMergePipelineFinishHook(IMergePipelineFinishHook hook) {
		incrementalMergeState.registerMergePipelineFinishHook(hook);
	}

	@Override
	public void unregisterMergePipelineFinishHook(IMergePipelineFinishHook hook) {
		incrementalMergeState.unregisterMergePipelineFinishHook(hook);
	}
}
