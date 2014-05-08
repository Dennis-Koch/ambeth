using System;

namespace De.Osthus.Ambeth.Testutil
{
    [AttributeUsage(AttributeTargets.Method | AttributeTargets.Class, Inherited = false, AllowMultiple = false)]
    public class TestRebuildContext : Attribute
    {
        public bool Value { get; private set; }

        public TestRebuildContext()
        {
            Value = true;
        }
    }
}
