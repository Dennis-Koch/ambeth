using System.Threading;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Security;

namespace De.Osthus.Ambeth.Security
{
    public class SecurityActivation
    {
        protected static readonly ThreadLocal<bool> securityActiveTL = new ThreadLocal<bool>(delegate()
            {
                return true;
            });

        public static bool IsSecured
        {
            get
            {
                return securityActiveTL.Value;
            }
        }

        public static void ExecuteWithoutSecurity(PausedSecurityRunnable pausedSecurityRunnable)
        {
            bool oldSecurityActive = securityActiveTL.Value;
            securityActiveTL.Value = false;
            try
            {
                pausedSecurityRunnable.Invoke();
            }
            finally
            {
                securityActiveTL.Value = oldSecurityActive;
            }
        }
    }
}
