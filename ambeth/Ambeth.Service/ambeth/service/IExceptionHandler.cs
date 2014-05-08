using System;
using System.Net;
using De.Osthus.Ambeth.Security;
using Castle.DynamicProxy;
using System.Threading;
using System.ServiceModel;
using System.Reflection;

namespace De.Osthus.Ambeth.Service
{
    public interface IExceptionHandler
    {
        Exception HandleException(MethodInfo method, Exception e);
    }
}
