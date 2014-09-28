using De.Osthus.Ambeth.Annotation;
using System;
using System.ComponentModel;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Helloworld.Transfer
{
    [DataContract(Namespace = "HelloWorld")]
    public class EmbeddedObject
    {
        [DataMember]
        [ParentChild]
        public virtual TestEntity2 RelationOfEmbeddedObject { get; set; }

        [DataMember]
        public virtual String Name { get; set; }

        [DataMember]
        public virtual int Value { get; set; }
    }
}
