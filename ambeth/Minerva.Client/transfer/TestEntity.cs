using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Helloworld.Model;
using System;
using System.Collections.Generic;
using System.Runtime.Serialization;

namespace De.Osthus.Ambeth.Helloworld.Transfer
{
    [DataContract(Namespace = "HelloWorld")]
    public class TestEntity : AbstractEntity
    {
        [DataMember]
        [ParentChild]
        public virtual TestEntity2 Relation { get; set; }

        [DataMember]
        [ParentChild]
        public virtual IList<TestEntity3> Relations { get; set; }

        private int test;

        [DataMember]
        public virtual int MyValue
        {
            get
            {
                return test;
            }
            set
            {
                test = value;
            }
        }

        [DataMember]
        public virtual String MyString { get; set; }

        [DataMember]
        public virtual EmbeddedObject EmbeddedObject { get; set; }

        /// <summary>
        /// The explicit PropertyChanged annotation will fire IN ADDITION to the default event to synchronize composite property values
        /// </summary>
        [IgnoreDataMember]
        [FireThisOnPropertyChange("MyValue")]
        public virtual int MyCompositeValueWithField
        {
            get
            {
                return MyValue + 5;
            }
        }
    }
}
