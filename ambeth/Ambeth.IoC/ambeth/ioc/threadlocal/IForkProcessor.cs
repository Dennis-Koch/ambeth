using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Ioc.Threadlocal
{
    public interface IForkProcessor
    {
        Object ResolveOriginalValue(Object bean, String fieldName, ThreadLocal<Object> fieldValueTL);

        Object CreateForkedValue(Object value);

        void ReturnForkedValue(Object value, Object forkedValue);
    }
}