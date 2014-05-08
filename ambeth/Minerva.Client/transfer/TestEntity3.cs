using De.Osthus.Ambeth.Helloworld.Model;
using System;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Helloworld.Transfer
{
    [DataContract(Namespace = "HelloWorld")]
    public class TestEntity3 : AbstractEntity
    {
        [DataMember]
        public virtual String MyValue3 { get; set; }

        [DataMember]
        public virtual TestEntity Test { get; set; }
    }
}
