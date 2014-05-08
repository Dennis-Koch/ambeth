using System;

namespace De.Osthus.Ambeth.Accessor
{
    public interface IAccessorTypeProvider
    {
	    AbstractAccessor GetAccessorType(Type type, String propertyName);

	    V GetConstructorType<V>(Type targetType);
    }
}
