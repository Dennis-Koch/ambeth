using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
#if !SILVERLIGHT
using System.Threading;
#else
#endif
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Threadlocal;

namespace De.Osthus.Ambeth.Security
{
    public class SecurityScopeProvider : IThreadLocalCleanupBean, ISecurityScopeProvider, ISecurityScopeChangeListenerExtendable
    {
        public static readonly ISecurityScope[] defaultSecurityScopes = new ISecurityScope[0];

        [Forkable]
        protected ThreadLocal<SecurityScopeHandle> securityScopeTL = new ThreadLocal<SecurityScopeHandle>();

        protected readonly DefaultExtendableContainer<ISecurityScopeChangeListener> securityScopeChangeListeners = new DefaultExtendableContainer<ISecurityScopeChangeListener>(
            "securityScopeChangeListener");

        public void CleanupThreadLocal()
        {
            securityScopeTL.Value = null;
        }

        public ISecurityScope[] SecurityScopes
        {
            get
            {
                SecurityScopeHandle securityScopeHandle = securityScopeTL.Value;
                if (securityScopeHandle == null)
                {
                    return defaultSecurityScopes;
                }
                if (securityScopeHandle.securityScopes == null)
                {
                    return defaultSecurityScopes;
                }
                return securityScopeHandle.securityScopes;
            }
            set
            {
                SecurityScopeHandle securityScopeHandle = securityScopeTL.Value;
                if (securityScopeHandle == null)
                {
                    securityScopeHandle = new SecurityScopeHandle();
                    securityScopeTL.Value = securityScopeHandle;
                }
                securityScopeHandle.securityScopes = value;
                NotifySecurityScopeChangeListeners(securityScopeHandle);
            }
        }

        public R ExecuteWithSecurityScopes<R>(IResultingBackgroundWorkerDelegate<R> runnable, params ISecurityScope[] securityScopes)
        {
            ISecurityScope[] oldSecurityScopes = SecurityScopes;
            try
            {
                SecurityScopes = securityScopes;
                return runnable();
            }
            finally
            {
                SecurityScopes = oldSecurityScopes;
            }
        }

        protected void NotifySecurityScopeChangeListeners(SecurityScopeHandle securityScopeHandle)
        {
	        foreach (ISecurityScopeChangeListener securityScopeChangeListener in securityScopeChangeListeners.GetExtensions())
	        {
		        securityScopeChangeListener.SecurityScopeChanged(securityScopeHandle.securityScopes);
	        }
        }

        public void RegisterSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener)
        {
            securityScopeChangeListeners.Register(securityScopeChangeListener);
        }

        public void UnregisterSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener)
        {
            securityScopeChangeListeners.Unregister(securityScopeChangeListener);
        }
    }
}