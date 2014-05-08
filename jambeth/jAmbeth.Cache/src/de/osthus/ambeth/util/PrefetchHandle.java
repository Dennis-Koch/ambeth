package de.osthus.ambeth.util;

import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;

public class PrefetchHandle implements IPrefetchHandle
{
	protected final IMap<Class<?>, List<String>> typeToMembersToInitialize;

	protected final IPrefetchHelper prefetchHelper;

	public PrefetchHandle(IMap<Class<?>, List<String>> typeToMembersToInitialize, IPrefetchHelper prefetchHelper)
	{
		this.typeToMembersToInitialize = typeToMembersToInitialize;
		this.prefetchHelper = prefetchHelper;
	}

	@Override
	public IPrefetchState prefetch(Object objects)
	{
		return prefetchHelper.prefetch(objects, typeToMembersToInitialize);
	}
	
	@Override
	public IPrefetchState prefetch(Object... objects)
	{
		return prefetchHelper.prefetch(objects, typeToMembersToInitialize);
	}

	@Override
	public IPrefetchHandle union(IPrefetchHandle otherPrefetchHandle)
	{
		if (otherPrefetchHandle == null)
		{
			return this;
		}
		HashMap<Class<?>, List<String>> unionMap = new HashMap<Class<?>, List<String>>();
		for (Entry<Class<?>, List<String>> entry : typeToMembersToInitialize)
		{
			unionMap.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
		}
		for (Entry<Class<?>, List<String>> entry : ((PrefetchHandle) otherPrefetchHandle).typeToMembersToInitialize)
		{
			List<String> prefetchPaths = unionMap.get(entry.getKey());
			if (prefetchPaths == null)
			{
				prefetchPaths = new ArrayList<String>();
				unionMap.put(entry.getKey(), prefetchPaths);
			}
			for (String prefetchPath : entry.getValue())
			{
				if (prefetchPaths.contains(prefetchPath))
				{
					continue;
				}
				prefetchPaths.add(prefetchPath);
			}
		}
		return new PrefetchHandle(unionMap, prefetchHelper);
	}
}
