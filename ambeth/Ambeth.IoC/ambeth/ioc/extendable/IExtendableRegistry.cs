using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public interface IExtendableRegistry
    {
        MethodInfo[] GetAddRemoveMethods(Type extendableInterface, Object[] arguments, out Object[] linkArguments);

        MethodInfo[] GetAddRemoveMethods(Type type, String eventName, Object[] arguments, out Object[] linkArguments);

        MethodInfo[] GetAddRemoveMethods(Type extendableInterface);

        MethodInfo[] GetAddRemoveMethods(Type extendableInterface, Type[] argumentTypes);

        [Obsolete]
        MethodInfo[] getAddRemoveMethods(Type extendableInterface, Object[] arguments, out Object[] linkArguments);

        [Obsolete]
        MethodInfo[] getAddRemoveMethodsForEvent(Type targetType, String eventName);
    }
}
