using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Security
{
    public interface IUserHandle
    {
        String SID { get; }

        bool IsValid { get; }

        ISecurityScope[] SecurityScopes { get; }

        IUseCase[] UseCases { get; }
    }
}
