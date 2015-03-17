using System;

namespace De.Osthus.Ambeth.Bytecode.Core
{
    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Method | AttributeTargets.Property | AttributeTargets.Constructor | AttributeTargets.Class, Inherited = false, AllowMultiple = false)]
    public class ByVisitor : Attribute
    {
        public String Value { get; set; }

        public ByVisitor(String value)
        {
            this.Value = Value;
        }
    }
}