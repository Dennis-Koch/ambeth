using System.Threading;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Merge.Config;
using System;
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
		protected readonly ThreadLocal<bool?> serviceActiveTL = new ThreadLocal<bool?>();

        [Forkable]
        protected readonly ThreadLocal<bool?> securityActiveTL = new ThreadLocal<bool?>();

        [Forkable]
        protected readonly ThreadLocal<bool?> entityActiveTL = new ThreadLocal<bool?>();

        [Property(MergeConfigurationConstants.SecurityActive, DefaultValue = "false")]
	    public bool SecurityActive { protected get; set; }

        public void CleanupThreadLocal()
        {
			if (securityActiveTL.Value != null || entityActiveTL.Value != null || serviceActiveTL.Value != null)
			{
				throw new Exception("Must be null at this point");
			}
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
				return EntitySecurityEnabled;
            }
        }

		public bool EntitySecurityEnabled
		{
			get
			{
				if (!SecurityActive)
				{
					return false;
				}
				bool? value = securityActiveTL.Value;
				if (value.HasValue && !value.Value)
				{
					return false;
				}
				value = entityActiveTL.Value;
				if (value.HasValue)
				{
					return value.Value;
				}
				return true;
			}
		}

		public bool ServiceSecurityEnabled
		{
			get
			{
				if (!SecurityActive)
				{
					return false;
				}
				bool? value = securityActiveTL.Value;
				if (value.HasValue && !value.Value)
				{
					return false;
				}
				value = securityActiveTL.Value;
				if (value.HasValue)
				{
					return value.Value;
				}
				return true;
			}
		}

		public bool ServiceOrEntitySecurityEnabled
		{
			get
			{
				return EntitySecurityEnabled || ServiceSecurityEnabled;
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
            bool? oldFilterActive = entityActiveTL.Value;
            entityActiveTL.Value = false;
            try
            {
                return noFilterRunnable();
            }
            finally
            {
                entityActiveTL.Value = oldFilterActive;
            }
        }

		public R ExecuteWithFiltering<R>(IResultingBackgroundWorkerDelegate<R> filterRunnable)
		{
			bool? oldFilterActive = entityActiveTL.Value;
			entityActiveTL.Value = true;
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
				entityActiveTL.Value = oldFilterActive;
			}
		}

		public void ExecuteWithSecurityDirective(SecurityDirective securityDirective, IBackgroundWorkerDelegate runnable)
		{
			bool? entityActive = securityDirective.HasFlag(SecurityDirective.DISABLE_ENTITY_CHECK) ? false : securityDirective
					.HasFlag(SecurityDirective.ENABLE_ENTITY_CHECK) ? true : default(bool?);
			bool? serviceActive = securityDirective.HasFlag(SecurityDirective.DISABLE_SERVICE_CHECK) ? false : securityDirective
					.HasFlag(SecurityDirective.ENABLE_SERVICE_CHECK) ? true : default(bool?);
			bool? oldEntityActive = null, oldServiceActive = null;
			if (entityActive != null)
			{
				oldEntityActive = entityActiveTL.Value;
				entityActiveTL.Value = entityActive;
			}
			try
			{
				if (serviceActive != null)
				{
					oldServiceActive = serviceActiveTL.Value;
					serviceActiveTL.Value = serviceActive;
				}
				try
				{
					runnable();
					return;
				}
				finally
				{
					if (serviceActive != null)
					{
						serviceActiveTL.Value = oldServiceActive;
					}
				}
			}
			finally
			{
				if (entityActive != null)
				{
					entityActiveTL.Value = oldEntityActive;
				}
			}
		}

		public R ExecuteWithSecurityDirective<R>(SecurityDirective securityDirective, IResultingBackgroundWorkerDelegate<R> runnable)
		{
			bool? entityActive = securityDirective.HasFlag(SecurityDirective.DISABLE_ENTITY_CHECK) ? false : securityDirective
					.HasFlag(SecurityDirective.ENABLE_ENTITY_CHECK) ? true : default(bool?);
			bool? serviceActive = securityDirective.HasFlag(SecurityDirective.DISABLE_SERVICE_CHECK) ? false : securityDirective
					.HasFlag(SecurityDirective.ENABLE_SERVICE_CHECK) ? true : default(bool?);
			bool? oldEntityActive = null, oldServiceActive = null;
			if (entityActive != null)
			{
				oldEntityActive = entityActiveTL.Value;
				entityActiveTL.Value = entityActive;
			}
			try
			{
				if (serviceActive != null)
				{
					oldServiceActive = serviceActiveTL.Value;
					serviceActiveTL.Value = serviceActive;
				}
				try
				{
					return runnable();
				}
				finally
				{
					if (serviceActive != null)
					{
						serviceActiveTL.Value = oldServiceActive;
					}
				}
			}
			finally
			{
				if (entityActive != null)
				{
					entityActiveTL.Value = oldEntityActive;
				}
			}
		}
    }
}
