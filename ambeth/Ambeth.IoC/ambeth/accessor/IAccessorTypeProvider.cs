using De.Osthus.Ambeth.Typeinfo;
using System;

namespace De.Osthus.Ambeth.Accessor
{
    public interface IAccessorTypeProvider
    {
        AbstractAccessor GetAccessorType(Type type, IPropertyInfo property);

	    V GetConstructorType<V>(Type targetType);
    }
}
