using System;
using System.Net;
using De.Osthus.Ambeth.Security;
using Castle.DynamicProxy;
using System.Threading;
using System.ServiceModel;
using De.Osthus.Ambeth.Service.Interceptor;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Service
{
    public interface IServiceFactory
    {
        I GetService<I>(params ISecurityScope[] securityScopes) where I : class;
    }
}
