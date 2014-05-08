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

namespace De.Osthus.Ambeth.Security
{
    public class SecurityScopeProvider : ISecurityScopeProvider
    {
        public class SecurityScopeHandle
        {
            public ISecurityScope[] securityScopes;

            public IUserHandle userHandle;
        }

        protected ThreadLocal<SecurityScopeHandle> securityScopeLocal = new ThreadLocal<SecurityScopeHandle>(delegate()
        {
            return new SecurityScopeHandle();
        });

        public ISecurityScope[] defaultSecurityScopes = new ISecurityScope[0];

        public ISecurityScope[] SecurityScopes {
            get {
                SecurityScopeHandle securityScopeHandle = securityScopeLocal.Value;
                if (securityScopeHandle.securityScopes == null)
                {
                    return defaultSecurityScopes;
                }
                return securityScopeHandle.securityScopes;
            }
            set {
                SecurityScopeHandle securityScopeHandle = securityScopeLocal.Value;
                securityScopeHandle.securityScopes = value;
            }
        }

        public IUserHandle UserHandle
        {
            get
            {
                SecurityScopeHandle securityScopeHandle = securityScopeLocal.Value;
                return securityScopeHandle.userHandle;
            }
            set
            {
                SecurityScopeHandle securityScopeHandle = securityScopeLocal.Value;
                securityScopeHandle.userHandle = value;
            }
        }
    }
}
