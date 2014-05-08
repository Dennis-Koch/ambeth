package de.osthus.ambeth.ioc.extendable;

import java.util.List;

import de.osthus.ambeth.collections.HashMap;

public class ClassEntry<V> extends HashMap<Class<?>, Object>
{
	public final HashMap<Class<?>, Object> typeToDefEntryMap = new HashMap<Class<?>, Object>(0.5f);

	public final HashMap<StrongKey<V>, List<DefEntry<V>>> definitionReverseMap = new HashMap<StrongKey<V>, List<DefEntry<V>>>(0.5f);

	public ClassEntry()
	{
		super(0.5f);
	}
}