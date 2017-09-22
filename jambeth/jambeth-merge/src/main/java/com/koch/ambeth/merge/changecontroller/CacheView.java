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

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.incremental.IMergePipelineFinishListener;
import com.koch.ambeth.merge.model.IChangeContainer;
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

	protected IdentityHashMap<Object, IChangeContainer> objectToChangeContainerMap;

	protected ArrayList<IMergeStepPreFlushListener> customRunnables;

	public CacheView(List<Object> newObjects, List<Object> oldObjects,
			List<IChangeContainer> changes, IIncrementalMergeState incrementalMergeState) {
		this.newObjects = newObjects;
		this.oldObjects = oldObjects;
		this.changes = changes;
		this.incrementalMergeState = incrementalMergeState;
	}

	@Override
	public IChangeContainer getChangeContainer(Object newOrOldObject) {
		if (objectToChangeContainerMap != null) {
			return objectToChangeContainerMap.get(newOrOldObject);
		}
		objectToChangeContainerMap = new IdentityHashMap<>();
		for (int a = newObjects.size(); a-- > 0;) {
			Object newObject = newObjects.get(a);
			Object oldObject = oldObjects.get(a);
			IChangeContainer changeContainer = changes.get(a);
			if (newObject != null) {
				objectToChangeContainerMap.put(newObject, changeContainer);
			}
			if (oldObject != null) {
				objectToChangeContainerMap.put(oldObject, changeContainer);
			}
		}
		return objectToChangeContainerMap.get(newOrOldObject);
	}

	/**
	 * Returns a list of new objects that have the given interface as type.
	 *
	 * @param clazz The interface that the object should implemented by the objects
	 * @return a list of new objects that implement the interface, never <code>null</code>
	 */
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
		return incrementalMergeState.getCustomState(key);
	}

	@Override
	public <T> T setCustomState(Object key, Object value) {
		return incrementalMergeState.setCustomState(key, value);
	}

	@Override
	public void queuePreFlush(IMergeStepPreFlushListener mergeStepPreFlushListener) {
		if (customRunnables == null) {
			customRunnables = new ArrayList<>();
		}
		customRunnables.add(mergeStepPreFlushListener);
	}

	public void processRunnables() {
		if (customRunnables == null) {
			return;
		}
		// while loop because a runnable could queue cascading runnables
		while (!customRunnables.isEmpty()) {
			IMergeStepPreFlushListener[] runnables =
					customRunnables.toArray(IMergeStepPreFlushListener.class);
			customRunnables.clear();
			try {
				for (IMergeStepPreFlushListener runnable : runnables) {
					runnable.preFlushStep(this);
				}
			}
			catch (Exception e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	@Override
	public void registerMergeProcessFinishListener(IMergePipelineFinishListener extension) {
		incrementalMergeState.registerMergeProcessFinishListener(extension);
	}

	@Override
	public void unregisterMergeProcessFinishListener(IMergePipelineFinishListener extension) {
		incrementalMergeState.unregisterMergeProcessFinishListener(extension);
	}
}
