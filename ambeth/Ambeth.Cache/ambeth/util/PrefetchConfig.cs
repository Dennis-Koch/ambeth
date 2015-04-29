using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchConfig : IPrefetchConfig
    {
        protected readonly HashMap<Type, List<String>> typeToMembersToInitialize = new HashMap<Type, List<String>>();

        [Autowired]
        public ICachePathHelper CachePathHelper { protected get; set; }

		[Autowired]
		public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public IPrefetchConfig Add(Type entityType, String propertyPath)
        {
			entityType = EntityMetaDataProvider.GetMetaData(entityType).EntityType;
			List<String> membersToInitialize = typeToMembersToInitialize.Get(entityType);
            if (membersToInitialize == null)
            {
                membersToInitialize = new List<String>();
                typeToMembersToInitialize.Put(entityType, membersToInitialize);
            }
            membersToInitialize.Add(propertyPath);
            return this;
        }

		public IPrefetchConfig Add(Type entityType, params String[] propertyPaths)
		{
			entityType = EntityMetaDataProvider.GetMetaData(entityType).EntityType;
			List<String> membersToInitialize = typeToMembersToInitialize.Get(entityType);
			if (membersToInitialize == null)
			{
				membersToInitialize = new List<String>();
				typeToMembersToInitialize.Put(entityType, membersToInitialize);
			}
			membersToInitialize.AddRange(propertyPaths);
			return this;
		}

        public IPrefetchHandle Build()
        {
            LinkedHashMap<Type, CachePath[]> entityTypeToPrefetchSteps = LinkedHashMap<Type, CachePath[]>.Create(typeToMembersToInitialize.Count);
            foreach (Entry<Type, List<String>> entry in typeToMembersToInitialize)
            {
                Type entityType = entry.Key;
				List<String> membersToInitialize = entry.Value;
                entityTypeToPrefetchSteps.Put(entityType, BuildCachePath(entityType, membersToInitialize));
            }
            return new PrefetchHandle(entityTypeToPrefetchSteps, CachePathHelper);
        }

        protected CachePath[] BuildCachePath(Type entityType, IList<String> membersToInitialize)
        {
            CHashSet<AppendableCachePath> cachePaths = new CHashSet<AppendableCachePath>();
            for (int a = membersToInitialize.Count; a-- > 0; )
            {
                String memberName = membersToInitialize[a];
                CachePathHelper.BuildCachePath(entityType, memberName, cachePaths);
            }
            return CachePathHelper.CopyAppendableToCachePath(cachePaths);
        }
    }
}
