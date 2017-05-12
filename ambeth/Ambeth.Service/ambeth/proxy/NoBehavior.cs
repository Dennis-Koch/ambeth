using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Proxy
{
    public class NoBehavior : IMethodLevelBehavior
    {
        public Object GetBehaviourOfMethod(MethodInfo method)
        {
            throw new NotSupportedException();
        }

        public Object GetDefaultBehaviour()
        {
            throw new NotSupportedException();
        }
    }
}