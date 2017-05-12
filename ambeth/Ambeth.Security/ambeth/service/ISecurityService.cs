using System;
using System.Net;
using System.ServiceModel;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Annotation;

namespace De.Osthus.Ambeth.Service
{
    [XmlType]
    public interface ISecurityService
    {
        Object CallServiceInSecurityScope(ISecurityScope[] securityScopes, IServiceDescription serviceDescription);
    }
}

