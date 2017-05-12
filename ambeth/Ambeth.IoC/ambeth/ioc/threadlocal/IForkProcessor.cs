using De.Osthus.Ambeth.Util;
using System;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public interface IForkProcessor
    {
        Object ResolveOriginalValue(Object bean, String fieldName, Object fieldValueTL);

        Object CreateForkedValue(Object value);

        void ReturnForkedValue(Object value, Object forkedValue);
    }
}