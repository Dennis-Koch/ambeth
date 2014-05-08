using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ServiceModel.Description;

namespace De.Osthus.Ambeth.Security
{
    [AttributeUsage(AttributeTargets.Interface | AttributeTargets.Method)]
    public class UserIndependentAttribute : Attribute
    {
    }
}
