using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Security.Transfer;
using System.Reflection;

namespace De.Osthus.Ambeth.Transfer
{
    [DataContract(Name = "ServiceDescription", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class ServiceDescription : IServiceDescription
    {
        [DataMember]
        public String ServiceName { get; set; }

        [DataMember]
        public String MethodName { get; set; }

        [DataMember]
        public Type[] ParamTypes { get; set; }

        [DataMember]
        public Object[] Arguments { get; set; }

        [DataMember]
        public ISecurityScope[] SecurityScopes { get; set; }

        [IgnoreDataMember]
        protected MethodInfo method;

        public MethodInfo GetMethod(Type serviceType)
        {
            if (method == null)
            {
                method = serviceType.GetMethod(StringConversionHelper.UpperCaseFirst(MethodName), ParamTypes);
                if (method == null)
                {
                    method = serviceType.GetMethod(StringConversionHelper.LowerCaseFirst(MethodName), ParamTypes);
                }
                if (method == null)
                {
                    throw new Exception("No matching method found on type " + serviceType.FullName + "'");
                }
            }
            return method;
        }
    }
}
