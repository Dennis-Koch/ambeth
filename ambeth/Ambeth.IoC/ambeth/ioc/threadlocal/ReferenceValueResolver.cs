using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ReferenceValueResolver : IForkedValueResolver
    {
        protected readonly Object forkedValue;

        public ReferenceValueResolver(Object forkedValue)
        {
            this.forkedValue = forkedValue;
        }

        public Object GetForkedValue()
        {
            return forkedValue;
        }
    }
}