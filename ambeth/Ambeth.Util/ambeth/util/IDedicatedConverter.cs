using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IDedicatedConverter
    {
        Object ConvertValueToType(Type expectedType, Type sourceType, Object value, Object additionalInformation);
    }
}