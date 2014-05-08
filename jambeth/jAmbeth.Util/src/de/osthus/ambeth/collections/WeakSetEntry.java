package de.osthus.ambeth.collections;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class WeakSetEntry<K> extends WeakReference<K> implements ISetEntry<K>, IPrintable
{
	protected final int hash;

	protected WeakSetEntry<K> nextEntry;

	public WeakSetEntry(K referent, int hash, WeakSetEntry<K> nextEntry, ReferenceQueue<? super K> referenceQueue)
	{
		super(referent, referenceQueue);
		this.hash = hash;
		this.nextEntry = nextEntry;
	}

	@Override
	public int getHash()
	{
		return hash;
	}

	@Override
	public K getKey()
	{
		return get();
	}

	@Override
	public WeakSetEntry<K> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(WeakSetEntry<K> nextEntry)
	{
		this.nextEntry = nextEntry;
	}

	@Override
	public String toString()
	{
		K key = get();
		if (key != null)
		{
			return key.toString();
		}
		return null;
	}

	@Override
	public void toString(StringBuilder sb)
	{
		StringBuilderUtil.appendPrintable(sb, getKey());
	}
}
