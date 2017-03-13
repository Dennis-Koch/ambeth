package com.koch.ambeth.util.collections;

import java.util.Collection;
import java.util.Iterator;

import com.koch.ambeth.util.StringBuilderUtil;

/**
 * Erweiterte Map, welche zus&auml;tzlich zu den &uuml;blichen Key/Value-Entries eine Liste aller Eintr&auml;ge verwaltet. Somit die Komplexit&auml;t f&uuml;r
 * das Iterieren &uuml;ber eine solchen Map mit O(n) = n identisch mit jener einer &uuml;blichen Array-Liste. Der Tradeoff sind hierbei nat&uuml;rlich de leicht
 * aufw&auml;ndigeren put()- und remove()-Operationen, welche jedoch weiterhin bzgl. der Komplexit&auml;t mit O(n) = 1 konstant bleiben.
 * 
 * @author kochd
 * 
 * @param <K>
 *            Der Typ der in der Map enthaltenen Keys
 * @param <V>
 *            Der Typ der in der Map enthaltenen Values
 */
public abstract class AbstractLinkedSet<K> extends AbstractHashSet<K> implements ILinkedSet<K>
{
	protected final GenericFastList<SetLinkedEntry<K>> fastIterationList;

	@SuppressWarnings("rawtypes")
	public AbstractLinkedSet(final Class<? extends ISetEntry> entryClass)
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, entryClass);
	}

	@SuppressWarnings("rawtypes")
	public AbstractLinkedSet(float loadFactor, final Class<? extends ISetEntry> entryClass)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor, entryClass);
	}

	@SuppressWarnings("rawtypes")
	public AbstractLinkedSet(int initialCapacity, final Class<? extends ISetEntry> entryClass)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR, entryClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AbstractLinkedSet(int initialCapacity, float loadFactor, final Class<? extends ISetEntry> entryClass)
	{
		super(initialCapacity, loadFactor, entryClass);
		fastIterationList = new GenericFastList(entryClass);
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 * 
	 * @return the number of key-value mappings in this map.
	 */
	@Override
	public final int size()
	{
		return fastIterationList.size();
	}

	@Override
	protected void entryAdded(final ISetEntry<K> entry)
	{
		fastIterationList.pushLast((SetLinkedEntry<K>) entry);
	}

	@Override
	protected void entryRemoved(final ISetEntry<K> entry)
	{
		fastIterationList.remove((SetLinkedEntry<K>) entry);
	}

	@Override
	protected void transfer(final ISetEntry<K>[] newTable)
	{
		int newCapacityMinus1 = newTable.length - 1;

		SetLinkedEntry<K> pointer = fastIterationList.getFirstElem(), next;
		while (pointer != null)
		{
			next = pointer.getNext();
			int i = pointer.getHash() & newCapacityMinus1;
			pointer.setNextEntry((SetLinkedEntry<K>) newTable[i]);
			newTable[i] = pointer;
			pointer = next;
		}
	}

	@Override
	public void toList(Collection<K> list)
	{
		SetLinkedEntry<K> pointer = fastIterationList.getFirstElem();
		while (pointer != null)
		{
			list.add(pointer.getKey());
			pointer = pointer.getNext();
		}
	}

	@Override
	protected void setNextEntry(ISetEntry<K> entry, ISetEntry<K> nextEntry)
	{
		((SetLinkedEntry<K>) entry).setNextEntry((SetLinkedEntry<K>) nextEntry);
	}

	@Override
	public SetLinkedIterator<K> iterator()
	{
		return new SetLinkedIterator<K>(this, true);
	}

	@Override
	public Iterator<K> iterator(boolean removeAllowed)
	{
		return new SetLinkedIterator<K>(this, removeAllowed);
	}

	@Override
	public void clear()
	{
		ISetEntry<K>[] table = this.table;
		int tableLengthMinusOne = table.length - 1;
		SetLinkedEntry<K> entry = fastIterationList.getFirstElem(), next;
		while (entry != null)
		{
			next = entry.getNext();
			int i = entry.getHash() & tableLengthMinusOne;
			table[i] = null;
			entryRemoved(entry);
			entry = next;
		}
		fastIterationList.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] array)
	{
		int index = 0;
		SetLinkedEntry<K> entry = fastIterationList.getFirstElem();
		while (entry != null)
		{
			array[index++] = (T) entry.getKey();
			entry = entry.getNext();
		}
		return array;
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(size()).append(" items: [");
		boolean first = true;
		SetLinkedEntry<K> pointer = fastIterationList.getFirstElem();
		while (pointer != null)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(',');
			}
			StringBuilderUtil.appendPrintable(sb, pointer);
			pointer = pointer.getNext();
		}
		sb.append(']');
	}
}
