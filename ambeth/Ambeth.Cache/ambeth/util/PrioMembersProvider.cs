using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Util
{
	public class PrioMembersProvider : IPrioMembersProvider
	{
		public static readonly String handleMetaDataAddedEvent = "HandleMetaDataAddedEvent";

		[LogInstance]
		public ILogger Log { private get; set; }

		[Autowired]
		public IEntityMetaDataProvider entityMetaDataProvider { protected get; set; }

		public readonly PrioMembersSmartCopyMap activeMembersToPrioMembersMap = new PrioMembersSmartCopyMap();

		public void HandleMetaDataAddedEvent(IEntityMetaDataEvent evnt)
		{
			activeMembersToPrioMembersMap.Clear();
		}

		protected bool IsPrio2Member(IEntityMetaData rootMetaData, IEntityMetaData metaData, PrefetchPath[] prefetchPaths,
				ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchSteps, Tuple2KeyHashMap<Type, PrefetchPath[], bool?> alreadyVisited,
				MergePrefetchPathsCache mergePrefetchPathsCache)
		{
			IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
			foreach (PrefetchPath prefetchPath in prefetchPaths)
			{
				if (!alreadyVisited.PutIfNotExists(prefetchPath.memberType, prefetchPath.children, true))
				{
					continue;
				}
				PrefetchPath[] children = mergePrefetchPathsCache.MergePrefetchPaths(prefetchPath.memberType, prefetchPath.children, entityTypeToPrefetchSteps);
				if (children == null)
				{
					continue;
				}
				IEntityMetaData childMetaData = entityMetaDataProvider.GetMetaData(prefetchPath.memberType);
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
				if (IsPrio2Member(rootMetaData, childMetaData, children, entityTypeToPrefetchSteps, alreadyVisited, mergePrefetchPathsCache))
				{
					return true;
				}
			}
			return false;
		}

		public IdentityLinkedSet<Member> GetPrioMembers(ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchPath,
				List<PrefetchCommand> pendingPrefetchCommands, MergePrefetchPathsCache mergePrefetchPathsCache)
		{
			IdentityLinkedSet<Member> key1 = new IdentityLinkedSet<Member>();
			PrioMembersKey key = new PrioMembersKey(entityTypeToPrefetchPath, key1);
			for (int a = 0, size = pendingPrefetchCommands.Count; a < size; a++)
			{
				PrefetchCommand prefetchCommand = pendingPrefetchCommands[a];
				key1.Add(prefetchCommand.valueHolder.Member);
			}
			IdentityLinkedSet<Member> prioMembersMap = activeMembersToPrioMembersMap.Get(key);
			if (prioMembersMap != null)
			{
				return prioMembersMap;
			}
			prioMembersMap = new IdentityLinkedSet<Member>(0.5f);
			Tuple2KeyHashMap<Type, PrefetchPath[], bool?> alreadyVisited = null;
			IdentityHashSet<Type> touchedTypesInPriority = null;

			if (mergePrefetchPathsCache == null)
			{
				mergePrefetchPathsCache = new MergePrefetchPathsCache(entityMetaDataProvider);
			}
			bool prio2Mode = true;
			foreach (PrefetchCommand prefetchCommand in pendingPrefetchCommands)
			{
				DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
				PrefetchPath[] prefetchPaths = prefetchCommand.prefetchPaths;
				RelationMember member = valueHolder.Member;

				Type targetEntityType = member.ElementType;
				// Merge the root prefetch path with the relative prefetch path
				prefetchPaths = mergePrefetchPathsCache.MergePrefetchPaths(targetEntityType, prefetchPaths, entityTypeToPrefetchPath);

				IEntityMetaData metaData = valueHolder.Vhc.Get__EntityMetaData();

				if (targetEntityType.Equals(metaData.EntityType))
				{
					// prio1 overrides prio2
					if (prio2Mode)
					{
						prio2Mode = false;
						alreadyVisited = null;
						prioMembersMap.Clear();
						if (touchedTypesInPriority != null)
						{
							touchedTypesInPriority.Clear();
						}
					}
					prioMembersMap.Add(member);
					if (touchedTypesInPriority == null)
					{
						touchedTypesInPriority = new IdentityHashSet<Type>();
					}
					touchedTypesInPriority.Add(member.EntityType);
					touchedTypesInPriority.Add(targetEntityType);
					continue;
				}
				if (prefetchPaths == null || !prio2Mode)
				{
					continue;
				}
				if (alreadyVisited == null)
				{
					alreadyVisited = new Tuple2KeyHashMap<Type, PrefetchPath[], bool?>();
				}
				if (IsPrio2Member(metaData, entityMetaDataProvider.GetMetaData(targetEntityType), prefetchPaths, entityTypeToPrefetchPath, alreadyVisited,
						mergePrefetchPathsCache))
				{
					prioMembersMap.Add(member);
					if (touchedTypesInPriority == null)
					{
						touchedTypesInPriority = new IdentityHashSet<Type>();
					}
					touchedTypesInPriority.Add(member.EntityType);
					touchedTypesInPriority.Add(targetEntityType);
				}
			}
			if (prioMembersMap.Count > 0)
			{
				// check for out-of-order members which have nothing to do (and will never ever have in a transitive manner) with the priorized members
				foreach (PrefetchCommand prefetchCommand in pendingPrefetchCommands)
				{
					DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
					RelationMember member = valueHolder.Member;

					if (prioMembersMap.Contains(member))
					{
						// already priorized
						continue;
					}
					if (touchedTypesInPriority.Contains(member.EntityType) || touchedTypesInPriority.Contains(member.ElementType))
					{
						continue;
					}
					prioMembersMap.Add(member);
				}
			}
			Object writeLock = activeMembersToPrioMembersMap.GetWriteLock();
			lock (writeLock)
			{
				IdentityLinkedSet<Member> existingPrioMembersMap = activeMembersToPrioMembersMap.Get(key);
				if (existingPrioMembersMap != null)
				{
					return existingPrioMembersMap;
				}
				activeMembersToPrioMembersMap.Put(key, prioMembersMap);
				return prioMembersMap;
			}
		}
	}
}