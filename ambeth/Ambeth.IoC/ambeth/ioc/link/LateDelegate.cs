using System;
namespace De.Osthus.Ambeth.Ioc.Link
{

    public class LateDelegate
    {
        protected String methodName;

        protected Type delegateType;

        public LateDelegate(Type delegateType, String methodName)
        {
            this.methodName = methodName;
            this.delegateType = delegateType;
        }

        public Delegate GetDelegate(Type delegateType, Object target)
        {
            if (this.delegateType != null)
            {
                delegateType = this.delegateType;
            }
            return Delegate.CreateDelegate(delegateType, target, methodName);
        }
    }
}