using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class GenericCollectionUtil
    {
        protected static ThreadLocal<IDictionary<Type, bool?>> typeToGenericCollectionDictTL = new ThreadLocal<IDictionary<Type, bool?>>(delegate()
        {
            return new Dictionary<Type, bool?>();
        });

        public static bool IsGenericList(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj is Type)
            {
                return IsGenericList((Type)obj);
            }
            return IsGenericList(obj.GetType());
        }

        public static bool IsGenericList(Type type)
        {
            if (!type.IsGenericType)
            {
                return false;
            }
            IDictionary<Type, bool?> typeToGenericCollectionDict = typeToGenericCollectionDictTL.Value;

            bool? result = DictionaryExtension.ValueOrDefault(typeToGenericCollectionDict, type);
            if (result == null)
            {
                result = false;
                Type[] typeInterfaces = type.GetInterfaces();
                foreach (Type typeInterface in typeInterfaces)
                {
                    if (!typeInterface.IsGenericType)
                    {
                        continue;
                    }
                    Type genericType = typeInterface.GetGenericTypeDefinition();
                    result = typeof(IList<>).IsAssignableFrom(genericType);
                    break;
                }
                typeToGenericCollectionDict.Add(type, result);
            }
            return result.Value;
        }
    }
}
