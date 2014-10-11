using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Threading;
#if !SILVERLIGHT
using System.Threading;
#else
using De.Osthus.Ambeth.Util;
#endif
using System;

namespace De.Osthus.Ambeth.Security
{
    public class SecurityContextHolder : IAuthorizationChangeListenerExtendable, ISecurityContextHolder
    {
        protected readonly DefaultExtendableContainer<IAuthorizationChangeListener> authorizationChangeListeners = new DefaultExtendableContainer<IAuthorizationChangeListener>("authorizationChangeListener");

        protected readonly ThreadLocal<ISecurityContext> contextTL = new ThreadLocal<ISecurityContext>();

        public void NotifyAuthorizationChangeListeners(IAuthorization authorization)
        {
            foreach (IAuthorizationChangeListener authorizationChangeListener in authorizationChangeListeners.GetExtensions())
            {
                authorizationChangeListener.AuthorizationChanged(authorization);
            }
        }

        public void RegisterAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener)
        {
            authorizationChangeListeners.Register(authorizationChangeListener);
        }

        public void UnregisterAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener)
        {
            authorizationChangeListeners.Unregister(authorizationChangeListener);
        }

        public ISecurityContext Context
        {
            get
            {
                return contextTL.Value;
            }
        }

        public ISecurityContext GetCreateContext()
        {
            ISecurityContext securityContext = Context;
            if (securityContext == null)
            {
                securityContext = new SecurityContextImpl(this);
                contextTL.Value = securityContext;
            }
            return securityContext;
        }

        public void ClearContext()
        {
            ISecurityContext securityContext = contextTL.Value;
            if (securityContext != null)
            {
                securityContext.Authentication = null;
                securityContext.Authorization = null;
                contextTL.Value = null;
            }
        }

        public R SetScopedAuthentication<R>(IAuthentication authentication, IResultingBackgroundWorkerDelegate<R> runnableScope)
        {
            ISecurityContext securityContext = Context;
            bool created = false;
            if (securityContext == null)
            {
                securityContext = GetCreateContext();
                created = true;
            }
            IAuthorization oldAuthorization = securityContext.Authorization;
            IAuthentication oldAuthentication = securityContext.Authentication;
            try
            {
                if (Object.ReferenceEquals(oldAuthentication, authentication))
                {
                    return runnableScope();
                }
                try
                {
                    securityContext.Authentication = authentication;
                    securityContext.Authorization = null;
                    return runnableScope();
                }
                finally
                {
                    securityContext.Authentication = oldAuthentication;
                    securityContext.Authorization = oldAuthorization;
                }
            }
            finally
            {
                if (created)
                {
                    ClearContext();
                }
            }
        }
    }
}