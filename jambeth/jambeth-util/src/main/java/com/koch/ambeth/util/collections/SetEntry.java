package com.koch.ambeth.util.collections;

import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public class SetEntry<K> implements ISetEntry<K>, IPrintable
{
	protected final int hash;

	protected SetEntry<K> nextEntry;

	protected final K key;

	public SetEntry(int hash, K key, SetEntry<K> nextEntry)
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
	public SetEntry<K> getNextEntry()
	{
		return nextEntry;
	}

	public void setNextEntry(SetEntry<K> nextEntry)
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
