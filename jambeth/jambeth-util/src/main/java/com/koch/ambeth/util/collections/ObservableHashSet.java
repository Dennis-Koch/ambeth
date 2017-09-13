package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util
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

import java.io.Externalizable;
import java.util.Collection;
import java.util.Set;

import com.koch.ambeth.util.collections.specialized.INotifyCollectionChangedListener;
import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedAction;
import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedEvent;
import com.koch.ambeth.util.collections.specialized.CollectionChangeSupport;

public class ObservableHashSet<V> extends HashSet<V> implements Externalizable, IObservableSet<V> {
	protected Object notifyCollectionChangedSupport;

	public ObservableHashSet() {
		super();
	}

	public ObservableHashSet(Collection<? extends V> sourceCollection) {
		super(sourceCollection);
	}

	public ObservableHashSet(float loadFactor) {
		super(loadFactor);
	}

	@SuppressWarnings("rawtypes")
	public ObservableHashSet(int initialCapacity, float loadFactor,
			Class<? extends ISetEntry> entryClass) {
		super(initialCapacity, loadFactor, entryClass);
	}

	public ObservableHashSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public ObservableHashSet(int initialCapacity) {
		super(initialCapacity);
	}

	public ObservableHashSet(Set<? extends V> map) {
		super(map);
	}

	public ObservableHashSet(V[] sourceArray) {
		super(sourceArray);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addNotifyCollectionChangedListener(INotifyCollectionChangedListener listener) {
		if (notifyCollectionChangedSupport == null) {
			notifyCollectionChangedSupport = listener;
			return;
		}
		if (!(notifyCollectionChangedSupport instanceof CollectionChangeSupport)) {
			INotifyCollectionChangedListener owner =
					(INotifyCollectionChangedListener) notifyCollectionChangedSupport;
			notifyCollectionChangedSupport = new CollectionChangeSupport();
			addNotifyCollectionChangedListener(owner);
		}
		((CollectionChangeSupport) notifyCollectionChangedSupport)
				.addNotifyCollectionChangedListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeNotifyCollectionChangedListener(INotifyCollectionChangedListener listener) {
		if (notifyCollectionChangedSupport == listener) {
			notifyCollectionChangedSupport = null;
			return;
		}
		if (!(notifyCollectionChangedSupport instanceof CollectionChangeSupport)) {
			return;
		}
		((CollectionChangeSupport) notifyCollectionChangedSupport)
				.removeNotifyCollectionChangedListener(listener);
	}

	protected void fireNotifyCollectionChanged(NotifyCollectionChangedEvent event) {
		if (notifyCollectionChangedSupport == null) {
			return;
		}
		if (!(notifyCollectionChangedSupport instanceof CollectionChangeSupport)) {
			INotifyCollectionChangedListener owner =
					(INotifyCollectionChangedListener) notifyCollectionChangedSupport;
			owner.collectionChanged(event);
			return;
		}
		((CollectionChangeSupport) notifyCollectionChangedSupport)
				.fireCollectionChange(event);
	}

	@Override
	protected boolean addIntern(V key) {
		if (!super.addIntern(key)) {
			return false;
		}
		if (notifyCollectionChangedSupport != null) {
			fireNotifyCollectionChanged(
					new NotifyCollectionChangedEvent(this, NotifyCollectionChangedAction.Add, key));
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		Object[] removedItems = toArray();
		super.clear();
		if (notifyCollectionChangedSupport != null && removedItems.length > 0) {
			fireNotifyCollectionChanged(new NotifyCollectionChangedEvent(this,
					NotifyCollectionChangedAction.Remove, removedItems));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected V removeEntryForKey(V key) {
		V entry = super.removeEntryForKey(key);
		if (entry == null) {
			return null;
		}
		if (notifyCollectionChangedSupport != null) {
			fireNotifyCollectionChanged(
					new NotifyCollectionChangedEvent(this, NotifyCollectionChangedAction.Remove, key));
		}
		return entry;
	}
}
