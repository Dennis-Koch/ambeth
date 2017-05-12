using System;
using System.Net;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeExtension
    {
        bool HandlesType(Type type);

        bool EqualsObjects(Object left, Object right);

        Object ExtractPrimitiveValueToMerge(Object value);
    }
}
