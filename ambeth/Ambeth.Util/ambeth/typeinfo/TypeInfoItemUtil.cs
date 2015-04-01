using De.Osthus.Ambeth.Collections;
using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Typeinfo
{
    public sealed class TypeInfoItemUtil
    {
        public static Type GetElementTypeUsingReflection(Type type, Type genericType_onlyUsedInJava)
        {
			if (type.HasElementType)
			{
				return type.GetElementType();
			}
			else if (!type.IsGenericType)
			{
				return type;
			}
			Type genericTypeDef = type.GetGenericTypeDefinition();
			if (typeof(IList<>).IsAssignableFrom(genericTypeDef))
			{
				return type.GetGenericArguments()[0];
			}
			if (typeof(IMap<,>).IsAssignableFrom(genericTypeDef))
			{
				return GetElementTypeUsingReflection(type.GetGenericArguments()[1], null);
			}
            return type;
        }

        private TypeInfoItemUtil()
        {
            // intended blank
        }
    }
}
