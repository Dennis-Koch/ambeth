package de.osthus.ambeth.merge.incremental;

import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ICUDResultHelper;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.util.IConversionHelper;

public class IncrementalMergeState implements IIncrementalMergeState
{
	public static class StateEntry
	{
		public final Object entity;

		public final IObjRef objRef;

		public final int index;

		public StateEntry(Object entity, IObjRef objRef, int index)
		{
			this.entity = entity;
			this.objRef = objRef;
			this.index = index;
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected ICUDResultHelper cudResultHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Property
	protected ICache stateCache;

	public final IdentityHashMap<Object, StateEntry> entityToStateMap = new IdentityHashMap<Object, StateEntry>();

	public final HashMap<IObjRef, StateEntry> objRefToStateMap = new HashMap<IObjRef, StateEntry>();

	private final HashMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap = new HashMap<Class<?>, HashMap<String, Integer>>();

	private final HashMap<Class<?>, HashMap<String, Integer>> typeToPrimitiveMemberNameToIndexMap = new HashMap<Class<?>, HashMap<String, Integer>>();

	private final Lock writeLock = new ReentrantLock();

	public final Comparator<IObjRef> objRefComparator = new Comparator<IObjRef>()
	{
		@Override
		public int compare(IObjRef o1, IObjRef o2)
		{
			int result = o1.getRealType().getName().compareTo(o2.getRealType().getName());
			if (result != 0)
			{
				return result;
			}
			String o1_id = conversionHelper.convertValueToType(String.class, o1.getId());
			String o2_id = conversionHelper.convertValueToType(String.class, o2.getId());
			if (o1_id != null && o2_id != null)
			{
				return o1_id.compareTo(o2_id);
			}
			if (o1_id == null && o2_id == null)
			{
				int o1Index = objRefToStateMap.get(o1).index;
				int o2Index = objRefToStateMap.get(o2).index;
				if (o1Index == o2Index)
				{
					return 0;
				}
				else if (o1Index < o2Index)
				{
					return -1;
				}
				return 1;
			}
			if (o1_id == null)
			{
				return 1;
			}
			return -1;
		}
	};

	@Override
	public ICache getStateCache()
	{
		return stateCache;
	}

	protected HashMap<String, Integer> getOrCreateRelationMemberNameToIndexMap(Class<?> entityType,
			IMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap)
	{
		HashMap<String, Integer> memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
		if (memberNameToIndexMap != null)
		{
			return memberNameToIndexMap;
		}
		writeLock.lock();
		try
		{
			memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
			if (memberNameToIndexMap != null)
			{
				// concurrent thread might have been faster
				return memberNameToIndexMap;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			RelationMember[] relationMembers = metaData.getRelationMembers();
			memberNameToIndexMap = HashMap.create(relationMembers.length);
			for (int a = relationMembers.length; a-- > 0;)
			{
				memberNameToIndexMap.put(relationMembers[a].getName(), Integer.valueOf(a));
			}
			typeToMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
			return memberNameToIndexMap;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected HashMap<String, Integer> getOrCreatePrimitiveMemberNameToIndexMap(Class<?> entityType,
			IMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap)
	{
		HashMap<String, Integer> memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
		if (memberNameToIndexMap != null)
		{
			return memberNameToIndexMap;
		}
		writeLock.lock();
		try
		{
			memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
			if (memberNameToIndexMap != null)
			{
				// concurrent thread might have been faster
				return memberNameToIndexMap;
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
			memberNameToIndexMap = HashMap.create(primitiveMembers.length);
			for (int a = primitiveMembers.length; a-- > 0;)
			{
				memberNameToIndexMap.put(primitiveMembers[a].getName(), Integer.valueOf(a));
			}
			typeToMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
			return memberNameToIndexMap;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public CreateOrUpdateContainerBuild newCreateContainer(Class<?> entityType)
	{
		return new CreateOrUpdateContainerBuild(true, getOrCreateRelationMemberNameToIndexMap(entityType, typeToMemberNameToIndexMap),
				getOrCreatePrimitiveMemberNameToIndexMap(entityType, typeToPrimitiveMemberNameToIndexMap), cudResultHelper);
	}

	@Override
	public CreateOrUpdateContainerBuild newUpdateContainer(Class<?> entityType)
	{
		return new CreateOrUpdateContainerBuild(false, getOrCreateRelationMemberNameToIndexMap(entityType, typeToMemberNameToIndexMap),
				getOrCreatePrimitiveMemberNameToIndexMap(entityType, typeToPrimitiveMemberNameToIndexMap), cudResultHelper);
	}
}
