using System;
using System.Collections;

namespace De.Osthus.Ambeth.Mapping
{
    public interface IListTypeHelper
    {
        bool IsListType(Type itemType);

        Object PackInListType(IEnumerable referencedVOs, Type listType);

        Object UnpackListType(Object item);
    }
}
