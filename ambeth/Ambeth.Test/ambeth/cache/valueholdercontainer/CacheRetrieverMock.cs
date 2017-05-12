using System;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Testutil;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    public class CacheRetrieverMock : AbstractCacheRetrieverMock, ICacheRetriever
    {
        [Autowired]
        public Object Reader { protected get; set; }

        public override void Initialize()
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(typeof(Material));

            LoadContainer lc = new LoadContainer();
            lc.Reference = new ObjRef(typeof(Material), 1, 1);
            lc.Primitives = new Object[metaData.PrimitiveMembers.Length];
            lc.Relations = new IObjRef[metaData.RelationMembers.Length][];

            lc.Primitives[metaData.GetIndexByPrimitiveName("Name")] = "Name1";

            databaseMap.Put(lc.Reference, lc);

            IEntityMetaData metaData2 = EntityMetaDataProvider.GetMetaData(typeof(MaterialType));
            LoadContainer lc2 = new LoadContainer();
            lc2.Reference = new ObjRef(typeof(MaterialType), 2, 1);
            lc2.Primitives = new Object[metaData2.PrimitiveMembers.Length];
            lc2.Relations = new IObjRef[metaData2.RelationMembers.Length][];

            lc2.Primitives[metaData2.GetIndexByPrimitiveName("Name")] = "Name2";

            lc.Relations[metaData.GetIndexByRelationName("Types")] = new IObjRef[] { lc2.Reference };

            databaseMap.Put(lc2.Reference, lc2);
        }
    }
}