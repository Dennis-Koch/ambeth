using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Privilege;
using De.Osthus.Ambeth.Privilege.Model;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Privilege
{
    public interface IPrivilegeProvider
    {
        void BuildPrefetchConfig(Type entityType, IPrefetchConfig prefetchConfig);

        bool IsCreateAllowed(Object entity, params ISecurityScope[] securityScopes);

        bool IsUpdateAllowed(Object entity, params ISecurityScope[] securityScopes);

        bool IsDeleteAllowed(Object entity, params ISecurityScope[] securityScopes);

        bool IsReadAllowed(Object entity, params ISecurityScope[] securityScopes);

        IPrivilegeItem GetPrivileges(Object entity, params ISecurityScope[] securityScopes);

        IList<IPrivilegeItem> GetPrivileges(IList<IObjRef> objRefs, params ISecurityScope[] securityScopes);
    }
}
