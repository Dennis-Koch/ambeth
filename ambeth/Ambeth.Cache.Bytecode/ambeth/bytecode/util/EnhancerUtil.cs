using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Util
{
    public class EnhancerUtil
    {
        public static MethodInstance GetSuperSetter(PropertyInstance propertyInfo)
        {
            IBytecodeBehaviorState state = BytecodeBehaviorState.State;
            Type superType = state.CurrentType;
            Type[] parameters = new Type[propertyInfo.Setter.Parameters.Length];
            for (int a = parameters.Length; a-- > 0; )
            {
                parameters[a] = propertyInfo.Setter.Parameters[a].Type;
            }

            MethodInfo superSetter = superType.GetMethod(propertyInfo.Setter.Name, BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, CallingConventions.HasThis, parameters, null);
            if (superSetter == null)
            {
#if SILVERLIGHT
                throw new MissingMethodException(superType.FullName + "." + propertyInfo.Setter.Name);
#else
                throw new MissingMethodException(superType.FullName, propertyInfo.Setter.Name);
#endif
            }
            return new MethodInstance(superSetter);
        }

        public static MethodInstance GetSuperGetter(PropertyInstance propertyInfo)
        {
            IBytecodeBehaviorState state = BytecodeBehaviorState.State;
            Type superType = state.CurrentType;

            MethodInfo superGetter = superType.GetMethod(GetGetterNameOfRelationPropertyWithNoInit(propertyInfo.Name), BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, CallingConventions.HasThis, new Type[0], null);
            if (superGetter == null)
            {
                // not a relation -> no lazy loading
                superGetter = superType.GetMethod(propertyInfo.Getter.Name, BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic, null, CallingConventions.HasThis, new Type[0], null);
                if (superGetter == null)
                {
#if SILVERLIGHT
                    throw new MissingMethodException(superType.FullName + "." + propertyInfo.Getter.Name);
#else
                    throw new MissingMethodException(superType.FullName, propertyInfo.Getter.Name);
#endif
                }
            }
            return new MethodInstance(superGetter);
        }

        public static String GetGetterNameOfRelationPropertyWithNoInit(String propertyName)
        {
            return "get_" + propertyName + ValueHolderIEC.GetNoInitSuffix();
        }
    }
}