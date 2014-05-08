using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IDedicatedConverterExtendable
    {
        void RegisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Type sourceType, Type targetType);

        void UnregisterDedicatedConverter(IDedicatedConverter dedicatedConverter, Type sourceType, Type targetType);
    }
}