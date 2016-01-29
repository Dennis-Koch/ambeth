package de.osthus.ambeth.util;

import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;

public class PrioMembersProvider implements IPrioMembersProvider
{
	public static final String handleMetaDataAddedEvent = "handleMetaDataAddedEvent";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected final PrioMembersSmartCopyMap activeMembersToPrioMembersMap = new PrioMembersSmartCopyMap();

	public void handleMetaDataAddedEvent(IEntityMetaDataEvent evnt)
	{
		activeMembersToPrioMembersMap.clear();
	}

	protected boolean isPrio2Member(IEntityMetaData rootMetaData, IEntityMetaData metaData, PrefetchPath[] prefetchPaths,
			ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps, Tuple2KeyHashMap<Class<?>, PrefetchPath[], Boolean> alreadyVisited,
			MergePrefetchPathsCache mergePrefetchPathsCache)
	{
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		for (PrefetchPath prefetchPath : prefetchPaths)
		{
			if (!alreadyVisited.putIfNotExists(prefetchPath.memberType, prefetchPath.children, Boolean.TRUE))
			{
				continue;
			}
			PrefetchPath[] children = mergePrefetchPathsCache.mergePrefetchPaths(prefetchPath.memberType, prefetchPath.children, entityTypeToPrefetchSteps);
			if (children == null)
			{
				continue;
			}
			IEntityMetaData childMetaData = entityMetaDataProvider.getMetaData(prefetchPath.memberType);
			if (metaData == childMetaData)
			{
				// prio1 case at a later stage
				continue;
			}
			if (rootMetaData == childMetaData)
			{
				// prio1 in a transitive manner (current stage)
				return true;
			}
			if (isPrio2Member(rootMetaData, childMetaData, children, entityTypeToPrefetchSteps, alreadyVisited, mergePrefetchPathsCache))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IdentityLinkedSet<Member> getPrioMembers(ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPath,
			ArrayList<PrefetchCommand> pendingPrefetchCommands, MergePrefetchPathsCache mergePrefetchPathsCache)
	{
		IdentityLinkedSet<Member> key1 = new IdentityLinkedSet<Member>();
		PrioMembersKey key = new PrioMembersKey(entityTypeToPrefetchPath, key1);
		for (int a = 0, size = pendingPrefetchCommands.size(); a < size; a++)
		{
			PrefetchCommand prefetchCommand = pendingPrefetchCommands.get(a);
			key1.add(prefetchCommand.valueHolder.getMember());
		}
		IdentityLinkedSet<Member> prioMembersMap = activeMembersToPrioMembersMap.get(key);
		if (prioMembersMap != null)
		{
			return prioMembersMap;
		}
		prioMembersMap = new IdentityLinkedSet<Member>(0.5f);
		Tuple2KeyHashMap<Class<?>, PrefetchPath[], Boolean> alreadyVisited = null;
		IdentityHashSet<Class<?>> touchedTypesInPriority = null;

		if (mergePrefetchPathsCache == null)
		{
			mergePrefetchPathsCache = new MergePrefetchPathsCache(entityMetaDataProvider);
		}
		boolean prio2Mode = true;
		for (PrefetchCommand prefetchCommand : pendingPrefetchCommands)
		{
			DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
			PrefetchPath[] prefetchPaths = prefetchCommand.prefetchPaths;
			RelationMember member = valueHolder.getMember();

			Class<?> targetEntityType = member.getElementType();
			// Merge the root prefetch path with the relative prefetch path
			prefetchPaths = mergePrefetchPathsCache.mergePrefetchPaths(targetEntityType, prefetchPaths, entityTypeToPrefetchPath);

			IEntityMetaData metaData = valueHolder.vhc.get__EntityMetaData();

			if (targetEntityType.equals(metaData.getEntityType()))
			{
				// prio1 overrides prio2
				if (prio2Mode)
				{
					prio2Mode = false;
					alreadyVisited = null;
					prioMembersMap.clear();
					if (touchedTypesInPriority != null)
					{
						touchedTypesInPriority.clear();
					}
				}
				prioMembersMap.add(member);
				if (touchedTypesInPriority == null)
				{
					touchedTypesInPriority = new IdentityHashSet<Class<?>>();
				}
				touchedTypesInPriority.add(member.getEntityType());
				touchedTypesInPriority.add(targetEntityType);
				continue;
			}
			if (prefetchPaths == null || !prio2Mode)
			{
				continue;
			}
			if (alreadyVisited == null)
			{
				alreadyVisited = new Tuple2KeyHashMap<Class<?>, PrefetchPath[], Boolean>();
			}
			if (isPrio2Member(metaData, entityMetaDataProvider.getMetaData(targetEntityType), prefetchPaths, entityTypeToPrefetchPath, alreadyVisited,
					mergePrefetchPathsCache))
			{
				prioMembersMap.add(member);
				if (touchedTypesInPriority == null)
				{
					touchedTypesInPriority = new IdentityHashSet<Class<?>>();
				}
				touchedTypesInPriority.add(member.getEntityType());
				touchedTypesInPriority.add(targetEntityType);
			}
		}
		if (prioMembersMap.size() > 0)
		{
			// check for out-of-order members which have nothing to do (and will never ever have in a transitive manner) with the priorized members
			for (PrefetchCommand prefetchCommand : pendingPrefetchCommands)
			{
				DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
				RelationMember member = valueHolder.getMember();

				if (prioMembersMap.contains(member))
				{
					// already priorized
					continue;
				}
				if (touchedTypesInPriority.contains(member.getEntityType()) || touchedTypesInPriority.contains(member.getElementType()))
				{
					continue;
				}
				prioMembersMap.add(member);
			}
		}
		Lock writeLock = activeMembersToPrioMembersMap.getWriteLock();
		writeLock.lock();
		try
		{
			IdentityLinkedSet<Member> existingPrioMembersMap = activeMembersToPrioMembersMap.get(key);
			if (existingPrioMembersMap != null)
			{
				return existingPrioMembersMap;
			}
			activeMembersToPrioMembersMap.put(key, prioMembersMap);
			return prioMembersMap;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
