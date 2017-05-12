using System;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Test.Model;
using De.Osthus.Ambeth.Testutil;

namespace De.Osthus.Ambeth.Xml.Test
{
    public class CacheRetrieverMock : AbstractCacheRetrieverMock, ICacheRetriever
    {
        public override void Initialize()
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(typeof(Material));

            LoadContainer lc = new LoadContainer();
            lc.Reference = new ObjRef(typeof(Material), 1, 1);
            lc.Primitives = new Object[metaData.PrimitiveMembers.Length];
            lc.Relations = new IObjRef[metaData.RelationMembers.Length][];
            lc.Primitives[metaData.GetIndexByPrimitiveName("Buid")] = "Material 1";
            lc.Primitives[metaData.GetIndexByPrimitiveName("Name")] = "Material 1";
            databaseMap.Put(lc.Reference, lc);

            IEntityMetaData metaData2 = EntityMetaDataProvider.GetMetaData(typeof(MaterialGroup));
            LoadContainer lc2 = new LoadContainer();
            lc2.Reference = new ObjRef(typeof(MaterialGroup), "1", 1);
            lc2.Primitives = new Object[metaData2.PrimitiveMembers.Length];
            lc2.Relations = new IObjRef[metaData2.RelationMembers.Length][];
            lc2.Primitives[metaData2.GetIndexByPrimitiveName("Buid")] = "MaterialGroup 1";
            lc2.Primitives[metaData2.GetIndexByPrimitiveName("Name")] = "MaterialGroup 1";
            databaseMap.Put(lc2.Reference, lc2);

            lc.Relations[metaData.GetIndexByRelationName("MaterialGroup")] = new IObjRef[] { lc2.Reference };

            IEntityMetaData metaData3 = EntityMetaDataProvider.GetMetaData(typeof(EntityA));
            LoadContainer lc3 = new LoadContainer();
            lc3.Reference = new ObjRef(typeof(EntityA), 1, 1);
            lc3.Primitives = new Object[metaData3.PrimitiveMembers.Length];
            lc3.Relations = new IObjRef[metaData3.RelationMembers.Length][];
            databaseMap.Put(lc3.Reference, lc3);

            IEntityMetaData metaData4 = EntityMetaDataProvider.GetMetaData(typeof(EntityB));
            LoadContainer lc4 = new LoadContainer();
            lc4.Reference = new ObjRef(typeof(EntityB), 1, 1);
            lc4.Primitives = new Object[metaData4.PrimitiveMembers.Length];
            lc4.Relations = new IObjRef[metaData4.RelationMembers.Length][];
            lc4.Relations[metaData4.GetIndexByRelationName("EntityA")] = new IObjRef[] { lc3.Reference };
            databaseMap.Put(lc4.Reference, lc4);

            lc3.Relations[metaData3.GetIndexByRelationName("EntityBs")] = new IObjRef[] { lc4.Reference };
        }
    }
}