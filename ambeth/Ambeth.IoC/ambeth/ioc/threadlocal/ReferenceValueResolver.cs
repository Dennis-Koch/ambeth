using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ReferenceValueResolver : IForkedValueResolver
    {
        protected readonly Object originalValue;

        protected readonly Object forkedValue;

        public ReferenceValueResolver(Object originalValue, Object forkedValue)
        {
            this.originalValue = originalValue;
            this.forkedValue = forkedValue;
        }

        public Object GetOriginalValue()
        {
            return originalValue;
        }

        public Object CreateForkedValue()
        {
            return forkedValue;
        }
    }
}