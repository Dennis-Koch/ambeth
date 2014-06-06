using System.Threading;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
#if SILVERLIGHT
using De.Osthus.Ambeth.Util;
#endif

namespace De.Osthus.Ambeth.Security
{
    public class SecurityActivation : ISecurityActivation
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly ThreadLocal<bool?> securityActiveTL = new ThreadLocal<bool?>();

        protected readonly ThreadLocal<bool?> filterActiveTL = new ThreadLocal<bool?>();

        public bool Secured
        {
            get
            {
                bool? value = securityActiveTL.Value;
                if (value == null)
                {
                    return true;
                }
                return value.Value;
            }
        }

        public bool FilterActivated
        {
            get
            {
                bool? value = filterActiveTL.Value;
                if (value == null)
                {
                    return true;
                }
                return value.Value;
            }
        }

        public R ExecuteWithoutSecurity<R>(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable)
        {
            bool? oldSecurityActive = securityActiveTL.Value;
            securityActiveTL.Value = false;
            try
            {
                return pausedSecurityRunnable();
            }
            finally
            {
                securityActiveTL.Value = oldSecurityActive;
            }
        }

        public R ExecuteWithoutFiltering<R>(IResultingBackgroundWorkerDelegate<R> noFilterRunnable)
        {
            bool? oldFilterActive = filterActiveTL.Value;
            filterActiveTL.Value = false;
            try
            {
                return noFilterRunnable();
            }
            finally
            {
                filterActiveTL.Value = oldFilterActive;
            }
        }
    }
}
