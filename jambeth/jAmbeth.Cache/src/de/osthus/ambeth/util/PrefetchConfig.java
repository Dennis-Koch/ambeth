package de.osthus.ambeth.util;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;

public class PrefetchConfig implements IPrefetchConfig
{
	protected final HashMap<Class<?>, List<String>> typeToMembersToInitialize = new HashMap<Class<?>, List<String>>();

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Override
	public IPrefetchConfig add(Class<?> entityType, String propertyPath)
	{
		List<String> membersToInitialize = typeToMembersToInitialize.get(entityType);
		if (membersToInitialize == null)
		{
			membersToInitialize = new ArrayList<String>();
			typeToMembersToInitialize.put(entityType, membersToInitialize);
		}
		membersToInitialize.add(propertyPath);
		return this;
	}

	@Override
	public IPrefetchHandle build()
	{
		return new PrefetchHandle(new HashMap<Class<?>, List<String>>(typeToMembersToInitialize), prefetchHelper);
	}
}
