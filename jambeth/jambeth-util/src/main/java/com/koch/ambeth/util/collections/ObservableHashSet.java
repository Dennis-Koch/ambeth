package com.koch.ambeth.util.collections;

import java.io.Externalizable;
import java.util.Collection;
import java.util.Set;

import com.koch.ambeth.util.collections.specialized.INotifyCollectionChangedListener;
import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedAction;
import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedEvent;
import com.koch.ambeth.util.collections.specialized.NotifyCollectionChangedSupport;

public class ObservableHashSet<V> extends HashSet<V> implements Externalizable, IObservableSet<V>
{
	protected Object notifyCollectionChangedSupport;

	public ObservableHashSet()
	{
		super();
	}

	public ObservableHashSet(Collection<? extends V> sourceCollection)
	{
		super(sourceCollection);
	}

	public ObservableHashSet(float loadFactor)
	{
		super(loadFactor);
	}

	@SuppressWarnings("rawtypes")
	public ObservableHashSet(int initialCapacity, float loadFactor, Class<? extends ISetEntry> entryClass)
	{
		super(initialCapacity, loadFactor, entryClass);
	}

	public ObservableHashSet(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public ObservableHashSet(int initialCapacity)
	{
		super(initialCapacity);
	}

	public ObservableHashSet(Set<? extends V> map)
	{
		super(map);
	}

	public ObservableHashSet(V[] sourceArray)
	{
		super(sourceArray);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addNotifyCollectionChangedListener(INotifyCollectionChangedListener listener)
	{
		if (notifyCollectionChangedSupport == null)
		{
			notifyCollectionChangedSupport = listener;
			return;
		}
		if (!(notifyCollectionChangedSupport instanceof NotifyCollectionChangedSupport))
		{
			INotifyCollectionChangedListener owner = (INotifyCollectionChangedListener) notifyCollectionChangedSupport;
			notifyCollectionChangedSupport = new NotifyCollectionChangedSupport();
			addNotifyCollectionChangedListener(owner);
		}
		((NotifyCollectionChangedSupport) notifyCollectionChangedSupport).addNotifyCollectionChangedListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeNotifyCollectionChangedListener(INotifyCollectionChangedListener listener)
	{
		if (notifyCollectionChangedSupport == listener)
		{
			notifyCollectionChangedSupport = null;
			return;
		}
		if (!(notifyCollectionChangedSupport instanceof NotifyCollectionChangedSupport))
		{
			return;
		}
		((NotifyCollectionChangedSupport) notifyCollectionChangedSupport).removeNotifyCollectionChangedListener(listener);
	}

	protected void fireNotifyCollectionChanged(NotifyCollectionChangedEvent event)
	{
		if (notifyCollectionChangedSupport == null)
		{
			return;
		}
		if (!(notifyCollectionChangedSupport instanceof NotifyCollectionChangedSupport))
		{
			INotifyCollectionChangedListener owner = (INotifyCollectionChangedListener) notifyCollectionChangedSupport;
			owner.collectionChanged(event);
			return;
		}
		((NotifyCollectionChangedSupport) notifyCollectionChangedSupport).fireNotifyCollectionChanged(event);
	}

	@Override
	protected boolean addIntern(V key)
	{
		if (!super.addIntern(key))
		{
			return false;
		}
		if (notifyCollectionChangedSupport != null)
		{
			fireNotifyCollectionChanged(new NotifyCollectionChangedEvent(this, NotifyCollectionChangedAction.Add, key));
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear()
	{
		Object[] removedItems = toArray();
		super.clear();
		if (notifyCollectionChangedSupport != null && removedItems.length > 0)
		{
			fireNotifyCollectionChanged(new NotifyCollectionChangedEvent(this, NotifyCollectionChangedAction.Remove, removedItems));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected V removeEntryForKey(V key)
	{
		V entry = super.removeEntryForKey(key);
		if (entry == null)
		{
			return null;
		}
		if (notifyCollectionChangedSupport != null)
		{
			fireNotifyCollectionChanged(new NotifyCollectionChangedEvent(this, NotifyCollectionChangedAction.Remove, key));
		}
		return entry;
	}
}
