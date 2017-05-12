using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Transfer
{
    [DataContract(Name = "MethodDescription", Namespace = "http://schemas.osthus.de/Ambeth")]
    public class MethodDescription : IMethodDescription
    {
        [DataMember]
        public Type ServiceType { get; set; }

        [DataMember]
        public String MethodName { get; set; }

        [IgnoreDataMember]
        protected MethodInfo method;

        [IgnoreDataMember]
        public MethodInfo Method {
            get
            {
                if (method == null)
                {
                    method = ServiceType.GetMethod(StringConversionHelper.UpperCaseFirst(MethodName), ParamTypes);
                    if (method == null)
                    {
                        method = ServiceType.GetMethod(StringConversionHelper.LowerCaseFirst(MethodName), ParamTypes);
                    }
                    if (method == null)
                    {
                        throw new Exception("No matching method found on type " + ServiceType.FullName + "'");
                    }
                }
                return method;
            }
        }

        [DataMember]
        public Type[] ParamTypes { get; set; }
    }
}
