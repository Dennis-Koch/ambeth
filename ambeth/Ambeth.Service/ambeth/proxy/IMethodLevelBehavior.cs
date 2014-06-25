using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public interface IMethodLevelBehavior<T> : IMethodLevelBehavior
    {
        new T GetDefaultBehaviour();

        new T GetBehaviourOfMethod(MethodInfo method);
    }

    public interface IMethodLevelBehavior
    {
        Object GetDefaultBehaviour();

        Object GetBehaviourOfMethod(MethodInfo method);
    }
}