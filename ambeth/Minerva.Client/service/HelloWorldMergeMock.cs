using De.Osthus.Ambeth.Helloworld.Transfer;
using De.Osthus.Minerva.Mock;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Helloworld.Service
{
    public class HelloWorldMergeMock : AbstractMergeServiceMock
    {
        protected int testEntitySeq = 0, testEntity2Seq = 0;

        protected override object AcquireIdForEntityType(Type entityType)
        {
            if (typeof(TestEntity).Equals(entityType))
            {
                return ++testEntitySeq;
            }
            else if (typeof(TestEntity2).Equals(entityType))
            {
                return ++testEntity2Seq;
            }
            else
            {
                throw new Exception("Entity type '" + entityType.Name + "' not supported");
            }
        }

        protected override void FillMetaData(Type entityType, IList<Type> typesRelatingToThis, IList<String> primitiveMembers,
            IList<String> relationMembers)
        {
            if (typeof(TestEntity).Equals(entityType))
            {
                primitiveMembers.Add("MyValue");
                primitiveMembers.Add("EmbeddedObject.Name");
                primitiveMembers.Add("EmbeddedObject.Value");
                relationMembers.Add("Relation");
            }
            else if (typeof(TestEntity2).Equals(entityType))
            {
                typesRelatingToThis.Add(typeof(TestEntity));
                primitiveMembers.Add("MyValue2");
            }
            else
            {
                throw new Exception("Entity type '" + entityType.Name + "' not supported");
            }
        }
    }
}
