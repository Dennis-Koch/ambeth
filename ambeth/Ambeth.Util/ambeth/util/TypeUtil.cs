using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Util
{
    public sealed class TypeUtil
    {
        public static NewType[] GetClassesToTypes(Type[] classes)
        {
            NewType[] types = new NewType[classes.Length];
            for (int a = classes.Length; a-- > 0; )
            {
                Type clazz = classes[a];
                if (clazz == null)
                {
                    continue;
                }
                types[a] = NewType.GetType(clazz);
            }
            return types;
        }

        public static Type[] GetParameterTypesToTypes(ParameterInfo[] classes)
        {
            Type[] types = new Type[classes.Length];
            for (int a = classes.Length; a-- > 0; )
            {
                types[a] = classes[a].ParameterType;
            }
            return types;
        }

        public static NewType[] GetClassesToTypes(ParameterInfo[] classes)
        {
            NewType[] types = new NewType[classes.Length];
            for (int a = classes.Length; a-- > 0; )
            {
                types[a] = NewType.GetType(classes[a].ParameterType);
            }
            return types;
        }

        public static Type[] GetClassesToTypesNative(ParameterInfo[] classes)
        {
            Type[] types = new Type[classes.Length];
            for (int a = classes.Length; a-- > 0; )
            {
                types[a] = classes[a].ParameterType;
            }
            return types;
        }

        private TypeUtil()
        {
            // Intended blank
        }
    }
}