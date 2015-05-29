using System.Threading;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Merge.Config;
#if SILVERLIGHT
using De.Osthus.Ambeth.Util;
#endif

namespace De.Osthus.Ambeth.Security
{
    public class SecurityActivation : ISecurityActivation, IThreadLocalCleanupBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Forkable]
        protected readonly ThreadLocal<bool?> securityActiveTL = new ThreadLocal<bool?>();

        [Forkable]
        protected readonly ThreadLocal<bool?> filterActiveTL = new ThreadLocal<bool?>();

        [Property(MergeConfigurationConstants.SecurityActive, DefaultValue = "false")]
	    public bool SecurityActive { protected get; set; }

        public void CleanupThreadLocal()
        {
            securityActiveTL.Value = null;
            filterActiveTL.Value = null;
        }

        public bool Secured
        {
            get
            {
                bool? value = securityActiveTL.Value;
                if (value == null)
                {
                    return SecurityActive;
                }
                return value.Value;
            }
        }

        public bool FilterActivated
        {
            get
            {
                if (!Secured)
                {
                    return false;
                }
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

		public R ExecuteWithFiltering<R>(IResultingBackgroundWorkerDelegate<R> filterRunnable)
		{
			bool? oldFilterActive = filterActiveTL.Value;
			filterActiveTL.Value = true;
			try
			{
				bool? oldSecurityActive = securityActiveTL.Value;
				securityActiveTL.Value = true;
				try
				{
					return filterRunnable();
				}
				finally
				{
					securityActiveTL.Value = oldSecurityActive;
				}
			}
			finally
			{
				filterActiveTL.Value = oldFilterActive;
			}
		}
    }
}
