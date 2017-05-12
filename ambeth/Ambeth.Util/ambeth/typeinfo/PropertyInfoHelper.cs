using System;
using System.Collections.Generic;
using System.Reflection;
using System.Collections;

namespace De.Osthus.Ambeth.Typeinfo
{
    public class PropertyInfoHelper
    {
        private IDictionary<Type, IDictionary<String, PropertyInfo>> propertyCache = new Dictionary<Type, IDictionary<String, PropertyInfo>>();

        public IDictionary<String, PropertyInfo> GetPropertyMap(Type type)
        {
            if (!propertyCache.ContainsKey(type))
            {
                PropertyInfo[] properties = type.GetProperties();
                IDictionary<String, PropertyInfo> propertyDict = new Dictionary<String, PropertyInfo>(properties.Length);

                for (int i = properties.Length; i-- > 0; )
                {
                    PropertyInfo property = properties[i];
                    propertyDict.Add(property.Name, property);
                }
                propertyCache.Add(type, propertyDict);
            }

            return propertyCache[type];
        }

        public Type GetElementType(PropertyInfo propertyInfo)
        {
            Type elementType = propertyInfo.PropertyType;
            if (typeof(ICollection).IsAssignableFrom(elementType))
            {
                elementType = elementType.GetElementType();
            }
            return elementType;
        }
    }
}
