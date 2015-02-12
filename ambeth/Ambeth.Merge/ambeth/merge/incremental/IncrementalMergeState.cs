using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Merge.Incremental
{
    public class IncrementalMergeState : IIncrementalMergeState
    {
        [LogInstance]
        public ILogger log { private get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public ICache StateCache { protected get; set; }

        public readonly IdentityHashMap<Object, StateEntry> entityToStateMap = new IdentityHashMap<Object, StateEntry>();

        public readonly HashMap<IObjRef, StateEntry> objRefToStateMap = new HashMap<IObjRef, StateEntry>();

        private readonly HashMap<Type, HashMap<String, int>> typeToMemberNameToIndexMap = new HashMap<Type, HashMap<String, int>>();

        private readonly HashMap<Type, HashMap<String, int>> typeToPrimitiveMemberNameToIndexMap = new HashMap<Type, HashMap<String, int>>();

        public readonly Comparison<IObjRef> objRefComparator;

        public IncrementalMergeState()
        {
            objRefComparator = new Comparison<IObjRef>(delegate(IObjRef o1, IObjRef o2)
            {
                int result = o1.GetType().FullName.CompareTo(o2.GetType().FullName);
                if (result != 0)
                {
                    return result;
                }
                String o1_id = ConversionHelper.ConvertValueToType<String>(o1.Id);
                String o2_id = ConversionHelper.ConvertValueToType<String>(o2.Id);
                if (o1_id != null && o2_id != null)
                {
                    return o1_id.CompareTo(o2_id);
                }
                if (o1_id == null && o2_id == null)
                {
                    int o1Index = objRefToStateMap.Get(o1).index;
                    int o2Index = objRefToStateMap.Get(o2).index;
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
            });
        }

        public ICache GetStateCache()
        {
            return StateCache;
        }

        protected HashMap<String, int> GetOrCreateRelationMemberNameToIndexMap(Type entityType,
                IMap<Type, HashMap<String, int>> typeToMemberNameToIndexMap)
        {
            HashMap<String, int> memberNameToIndexMap = typeToMemberNameToIndexMap.Get(entityType);
            if (memberNameToIndexMap != null)
            {
                return memberNameToIndexMap;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
            RelationMember[] relationMembers = metaData.RelationMembers;
            memberNameToIndexMap = HashMap<String, int>.Create(relationMembers.Length);
            for (int a = relationMembers.Length; a-- > 0; )
            {
                memberNameToIndexMap.Put(relationMembers[a].Name, a);
            }
            typeToMemberNameToIndexMap.Put(entityType, memberNameToIndexMap);
            return memberNameToIndexMap;
        }

        protected HashMap<String, int> GetOrCreatePrimitiveMemberNameToIndexMap(Type entityType,
                IMap<Type, HashMap<String, int>> typeToMemberNameToIndexMap)
        {
            HashMap<String, int> memberNameToIndexMap = typeToMemberNameToIndexMap.Get(entityType);
            if (memberNameToIndexMap != null)
            {
                return memberNameToIndexMap;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
            PrimitiveMember[] primitiveMembers = metaData.PrimitiveMembers;
            memberNameToIndexMap = HashMap<String, int>.Create(primitiveMembers.Length);
            for (int a = primitiveMembers.Length; a-- > 0; )
            {
                memberNameToIndexMap.Put(primitiveMembers[a].Name, a);
            }
            typeToMemberNameToIndexMap.Put(entityType, memberNameToIndexMap);
            return memberNameToIndexMap;
        }

        public CreateOrUpdateContainerBuild NewCreateContainer(Type entityType)
        {
            return new CreateOrUpdateContainerBuild(true, GetOrCreateRelationMemberNameToIndexMap(entityType, typeToMemberNameToIndexMap),
                    GetOrCreatePrimitiveMemberNameToIndexMap(entityType, typeToPrimitiveMemberNameToIndexMap));
        }

        public CreateOrUpdateContainerBuild NewUpdateContainer(Type entityType)
        {
            return new CreateOrUpdateContainerBuild(false, GetOrCreateRelationMemberNameToIndexMap(entityType, typeToMemberNameToIndexMap),
                    GetOrCreatePrimitiveMemberNameToIndexMap(entityType, typeToPrimitiveMemberNameToIndexMap));
        }
    }
}