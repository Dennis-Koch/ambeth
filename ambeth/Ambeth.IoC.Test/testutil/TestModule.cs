using System;

namespace De.Osthus.Ambeth.Testutil
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, Inherited = true, AllowMultiple = true)]
    public class TestModule : Attribute
    {
        public Type[] Value { get; private set; }

        public TestModule(params Type[] value)
        {
            this.Value = value;
        }
    }
}
