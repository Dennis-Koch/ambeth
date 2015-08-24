package de.osthus.ambeth.ioc.extendable;

import java.util.List;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.LinkedHashMap;

public class ClassEntry<V> extends HashMap<Class<?>, Object>
{
	public final LinkedHashMap<Class<?>, Object> typeToDefEntryMap = new LinkedHashMap<Class<?>, Object>(0.5f);

	public final LinkedHashMap<StrongKey<V>, List<DefEntry<V>>> definitionReverseMap = new LinkedHashMap<StrongKey<V>, List<DefEntry<V>>>(0.5f);

	public ClassEntry()
	{
		super(0.5f);
	}
}