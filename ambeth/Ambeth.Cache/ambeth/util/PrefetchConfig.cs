using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Merge.Model;
using Castle.DynamicProxy;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Util
{
    public class PrefetchConfig : IPrefetchConfig
    {
		public class PrefetchConfigInterceptor : IInterceptor
		{
			private readonly PrefetchConfig prefetchConfig;

			private readonly IEntityMetaData currMetaData;

			private readonly Type baseEntityType;

			private readonly String propertyPath;

			public PrefetchConfigInterceptor(PrefetchConfig prefetchConfig, IEntityMetaData currMetaData, Type baseEntityType, String propertyPath)
			{
				this.prefetchConfig = prefetchConfig;
				this.currMetaData = currMetaData;
				this.baseEntityType = baseEntityType;
				this.propertyPath = propertyPath;
			}

			public void Intercept(IInvocation invocation)
			{
				String propertyName = prefetchConfig.PropertyInfoProvider.GetPropertyNameFor(invocation.Method);
				if (propertyName == null)
				{
					return;
				}
				Member member = currMetaData.GetMemberByName(propertyName);
				IEntityMetaData targetMetaData = prefetchConfig.EntityMetaDataProvider.GetMetaData(member.ElementType);

				Object childPlan = prefetchConfig.PlanIntern(baseEntityType, propertyPath != null ? propertyPath + "." + propertyName : propertyName,
						targetMetaData);

				if (member.ElementType.Equals(member.RealType))
				{
					// to-one relation
					invocation.ReturnValue = childPlan;
					return;
				}
				Object list = ListUtil.CreateCollectionOfType(member.RealType, 1);
				ListUtil.FillList(list, new Object[] { childPlan });
				invocation.ReturnValue = list;
			}
		}

        [Autowired]
        public ICachePathHelper CachePathHelper { protected get; set; }

		[Autowired]
		public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

		[Autowired]
		public IPropertyInfoProvider PropertyInfoProvider{ protected get; set; }

		[Autowired]
		public IProxyFactory ProxyFactory{ protected get; set; }

		protected readonly HashMap<Type, List<String>> entityTypeToPrefetchPaths = new HashMap<Type, List<String>>();

		public T Plan<T>()
		{
			return (T)Plan(typeof(T));
		}

		public Object Plan(Type entityType)
		{
			return PlanIntern(entityType, null, EntityMetaDataProvider.GetMetaData(entityType));
		}

		protected Object PlanIntern(Type baseEntityType, String propertyPath, IEntityMetaData currMetaData)
		{
			if (propertyPath != null)
			{
				Add(baseEntityType, propertyPath);
			}
			return ProxyFactory.CreateProxy(currMetaData.EntityType, new PrefetchConfigInterceptor(this, currMetaData, baseEntityType, propertyPath));
		}

        public IPrefetchConfig Add(Type entityType, String propertyPath)
        {
			entityType = EntityMetaDataProvider.GetMetaData(entityType).EntityType;
			List<String> membersToInitialize = entityTypeToPrefetchPaths.Get(entityType);
            if (membersToInitialize == null)
            {
                membersToInitialize = new List<String>();
				entityTypeToPrefetchPaths.Put(entityType, membersToInitialize);
            }
            membersToInitialize.Add(propertyPath);
            return this;
        }

		public IPrefetchConfig Add(Type entityType, params String[] propertyPaths)
		{
			entityType = EntityMetaDataProvider.GetMetaData(entityType).EntityType;
			List<String> membersToInitialize = entityTypeToPrefetchPaths.Get(entityType);
			if (membersToInitialize == null)
			{
				membersToInitialize = new List<String>();
				entityTypeToPrefetchPaths.Put(entityType, membersToInitialize);
			}
			membersToInitialize.AddRange(propertyPaths);
			return this;
		}

        public IPrefetchHandle Build()
        {
			LinkedHashMap<Type, PrefetchPath[]> entityTypeToPrefetchSteps = LinkedHashMap<Type, PrefetchPath[]>.Create(entityTypeToPrefetchPaths.Count);
			foreach (Entry<Type, List<String>> entry in entityTypeToPrefetchPaths)
            {
                Type entityType = entry.Key;
				List<String> membersToInitialize = entry.Value;
                entityTypeToPrefetchSteps.Put(entityType, BuildCachePath(entityType, membersToInitialize));
            }
            return new PrefetchHandle(entityTypeToPrefetchSteps, CachePathHelper);
        }

        protected PrefetchPath[] BuildCachePath(Type entityType, IList<String> membersToInitialize)
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
