using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public class ShallowCopyValueResolver : IForkedValueResolver
    {
        protected readonly Object forkedValue;

        public ShallowCopyValueResolver(Object forkedValue)
        {
            this.forkedValue = forkedValue;
        }

        public Object GetForkedValue()
		{
			throw new NotSupportedException("Not yet implemented");
			//return ReflectUtil.GetDeclaredMethod(false, forkedValue.getClass(), null, "clone", new Class<?>[0]).invoke(forkedValue);
		}
    }
}
