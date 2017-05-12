using De.Osthus.Ambeth.Helloworld.Model;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Helloworld.Transfer
{
    [DataContract(Namespace = "HelloWorld")]
    public class TestEntity2 : AbstractEntity
    {
        [DataMember]
        public virtual int MyValue2 { get; set; }
    }
}
