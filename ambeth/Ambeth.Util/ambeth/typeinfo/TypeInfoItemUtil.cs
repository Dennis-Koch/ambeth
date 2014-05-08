using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Typeinfo
{
    public sealed class TypeInfoItemUtil
    {
        public static Type GetElementTypeUsingReflection(Type type, Type genericType_onlyUsedInJava)
        {
            if (type.IsGenericType)
            {
                Type genericTypeDef = type.GetGenericTypeDefinition();
                if (typeof(IList<>).IsAssignableFrom(genericTypeDef))
                {
                    return type.GetGenericArguments()[0];
                }
            }
            if (type.HasElementType)
            {
                return type.GetElementType();
            }
            return type;
        }

        private TypeInfoItemUtil()
        {
            // intended blank
        }
    }
}
