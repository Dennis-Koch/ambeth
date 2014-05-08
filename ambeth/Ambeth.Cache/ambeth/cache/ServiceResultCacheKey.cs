using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Transfer;
using De.Osthus.Ambeth.Util;
using System.Reflection;
using System.Threading;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Security;

namespace De.Osthus.Ambeth.Cache
{
    public class ServiceResultCacheKey
    {
        public MethodInfo Method;

        public IList<Object> Arguments;

        public String ServiceName;
        
        public override int GetHashCode()
        {
            return Method.GetHashCode() ^ ServiceName.GetHashCode();
        }

        public override bool Equals(object obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }
            if (!(obj is ServiceResultCacheKey))
            {
                return false;
            }
            ServiceResultCacheKey other = (ServiceResultCacheKey)obj;

            if (!Object.Equals(Method, other.Method) || !Object.Equals(ServiceName, other.ServiceName))
            {
                return false;
            }
            IList<Object> otherArgs = other.Arguments;
            for (int a = otherArgs.Count; a-- > 0; )
            {
                if (!Object.Equals(Arguments[a], otherArgs[a]))
                {
                    return false;
                }
            }
            return true;
        }
    }
}
