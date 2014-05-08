using System;
using System.ServiceModel;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Security.Transfer;
using De.Osthus.Ambeth.Transfer;

namespace De.Osthus.Ambeth.Service
{
    public class SecurityServiceDelegate : ISecurityService, IInitializingBean
    {
        public virtual ISecurityServiceWCF SecurityServiceWCF { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(SecurityServiceWCF, "SecurityServiceWCF");
        }

        public virtual Object CallServiceInSecurityScope(ISecurityScope[] securityScopes, IServiceDescription serviceDescription)
        {
            SecurityScope[] param1WCF = new SecurityScope[securityScopes.Length];
            for (int a = securityScopes.Length; a-- > 0;)
            {
                param1WCF[a] = (SecurityScope)securityScopes[a];
            }
            Object resultWCF = SecurityServiceWCF.CallServiceInSecurityScope(param1WCF, (ServiceDescription)serviceDescription);
            return resultWCF;
        }
    }
}
