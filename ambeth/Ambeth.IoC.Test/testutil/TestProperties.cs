using System;

namespace De.Osthus.Ambeth.Testutil
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, Inherited = false, AllowMultiple = true)]
    public class TestProperties : Attribute
    {
        public String File { get; set; }

        public String Name { get; set; }

        public String Value { get; set; }

        public Type Type { get; set; }

        public TestProperties()
        {
            File = "";
            Name = "";
            Value = "";
            Type = typeof(IPropertiesProvider);
        }
    }
}
