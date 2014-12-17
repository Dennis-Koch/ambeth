using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    [AttributeUsage(AttributeTargets.Field, Inherited = false, AllowMultiple = false)]
    public class Forkable : Attribute
    {
        public ForkableType Value { get; private set; }

        public Forkable()
            : this(ForkableType.REFERENCE)
        {
            // intended blank
        }

        public Forkable(ForkableType forkableType)
        {
            Value = forkableType;
        }
    }
}
