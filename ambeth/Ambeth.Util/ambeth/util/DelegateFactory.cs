using System;

namespace De.Osthus.Ambeth.Util
{
    public class DelegateFactory : IDelegateFactory
    {
        public Delegate CreateDelegate(Type delegateType, Object target, String methodName)
        {
            return Delegate.CreateDelegate(delegateType, target, methodName);
        }
    }
}
