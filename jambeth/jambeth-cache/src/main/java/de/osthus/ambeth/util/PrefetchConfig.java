package de.osthus.ambeth.util;

import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;

public class PrefetchConfig implements IPrefetchConfig
{
	protected final HashMap<Class<?>, ArrayList<String>> typeToMembersToInitialize = new HashMap<Class<?>, ArrayList<String>>();

	@Autowired
	protected ICachePathHelper cachePathHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public IPrefetchConfig add(Class<?> entityType, String propertyPath)
	{
		entityType = entityMetaDataProvider.getMetaData(entityType).getEntityType();
		ArrayList<String> membersToInitialize = typeToMembersToInitialize.get(entityType);
		if (membersToInitialize == null)
		{
			membersToInitialize = new ArrayList<String>();
			typeToMembersToInitialize.put(entityType, membersToInitialize);
		}
		membersToInitialize.add(propertyPath);
		return this;
	}

	@Override
	public IPrefetchConfig add(Class<?> entityType, String... propertyPaths)
	{
		entityType = entityMetaDataProvider.getMetaData(entityType).getEntityType();
		ArrayList<String> membersToInitialize = typeToMembersToInitialize.get(entityType);
		if (membersToInitialize == null)
		{
			membersToInitialize = new ArrayList<String>();
			typeToMembersToInitialize.put(entityType, membersToInitialize);
		}
		membersToInitialize.addAll(propertyPaths);
		return this;
	}

	@Override
	public IPrefetchHandle build()
	{
		LinkedHashMap<Class<?>, CachePath[]> entityTypeToPrefetchSteps = LinkedHashMap.create(typeToMembersToInitialize.size());
		for (Entry<Class<?>, ArrayList<String>> entry : typeToMembersToInitialize)
		{
			Class<?> entityType = entry.getKey();
			ArrayList<String> membersToInitialize = entry.getValue();
			entityTypeToPrefetchSteps.put(entityType, buildCachePath(entityType, membersToInitialize));
		}
		return new PrefetchHandle(entityTypeToPrefetchSteps, cachePathHelper);
	}

	protected CachePath[] buildCachePath(Class<?> entityType, List<String> membersToInitialize)
	{
		LinkedHashSet<AppendableCachePath> cachePaths = new LinkedHashSet<AppendableCachePath>();
		for (int a = membersToInitialize.size(); a-- > 0;)
		{
			String memberName = membersToInitialize.get(a);
			cachePathHelper.buildCachePath(entityType, memberName, cachePaths);
		}
		return cachePathHelper.copyAppendableToCachePath(cachePaths);
	}
}
