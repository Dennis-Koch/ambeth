package de.osthus.ambeth.collections;

import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class SetLinkedEntry<K> extends AbstractListElem<SetLinkedEntry<K>> implements ISetEntry<K>, IPrintable
{
	protected final int hash;

	protected SetLinkedEntry<K> nextEntry;

	protected final K key;

	public SetLinkedEntry()
	{
		// For GenericFastList
		hash = 0;
		key = null;
	}

	public SetLinkedEntry(int hash, K key, SetLinkedEntry<K> nextEntry)
	{
		this.hash = hash;
		this.key = key;
		this.nextEntry = nextEntry;
	}

	@Override
	public K getKey()
	{
		return key;
	}

	@Override
	public int getHash()
	{
		return hash;
	}

	@Override
	public SetLinkedEntry<K> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(SetLinkedEntry<K> nextEntry)
	{
		this.nextEntry = nextEntry;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		StringBuilderUtil.appendPrintable(sb, getKey());
	}
}
