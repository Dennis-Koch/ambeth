using System;

namespace De.Osthus.Ambeth.Util
{
    public interface IDelegateFactory
    {
	    Delegate CreateDelegate(Type delegateType, Object target, String methodName);
    }
}
