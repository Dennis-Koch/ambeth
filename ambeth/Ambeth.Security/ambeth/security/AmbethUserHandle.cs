using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public class AmbethUserHandle : IUserHandle
    {
        public String SID { get; private set; }

        public ISecurityScope[] SecurityScopes { get; private set; }

        public IUseCase[] UseCases { get; private set; }

        public bool IsValid
        {
            get
            {
                return false;
            }
        }

        public AmbethUserHandle(String sid, ISecurityScope[] securityScopes, IUseCase[] useCases)
        {
            SID = sid;
            this.SecurityScopes = securityScopes;
            this.UseCases = useCases;
        }
    }
}
