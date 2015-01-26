using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ShallowCopyValueResolver : IForkedValueResolver
    {
        protected readonly Object originalValue;

        public ShallowCopyValueResolver(Object forkedValue)
        {
            this.originalValue = forkedValue;
        }

        public Object CreateForkedValue()
		{
			throw new NotSupportedException("Not yet implemented");
			//return ReflectUtil.GetDeclaredMethod(false, forkedValue.getClass(), null, "clone", new Class<?>[0]).invoke(forkedValue);
		}

        public Object GetOriginalValue()
        {
            return originalValue;
        }
    }
}
