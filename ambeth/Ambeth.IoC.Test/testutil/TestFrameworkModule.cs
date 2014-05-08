using System;

namespace De.Osthus.Ambeth.Testutil
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, Inherited = true, AllowMultiple = true)]
    public class TestFrameworkModule : Attribute
    {
        public Type[] Value { get; private set; }

        public TestFrameworkModule(params Type[] value)
        {
            this.Value = value;
        }
    }
}
