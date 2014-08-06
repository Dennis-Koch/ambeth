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
        IPrivilege GetPrivilegeByObjRef(IObjRef objRef, params ISecurityScope[] securityScopes);

        /// <summary>
        /// Result correlates by-index with the given objRefs
        /// </summary>
        /// <typeparam name="V"></typeparam>
        /// <param name="entities"></param>
        /// <param name="securityScopes"></param>
        /// <returns></returns>
        IList<IPrivilege> GetPrivileges<V>(IEnumerable<V> entities, params ISecurityScope[] securityScopes);

        /// <summary>
        /// Result correlates by-index with the given objRefs
        /// </summary>
        /// <typeparam name="V"></typeparam>
        /// <param name="objRefs"></param>
        /// <param name="securityScopes"></param>
        /// <returns></returns>
        IList<IPrivilege> GetPrivilegesByObjRef<V>(IEnumerable<V> objRefs, params ISecurityScope[] securityScopes) where V : IObjRef;

        ITypePrivilege GetPrivilegeByType(Type entityType, params ISecurityScope[] securityScopes);

        IList<ITypePrivilege> GetPrivilegesByType(IEnumerable<Type> entityTypes, params ISecurityScope[] securityScopes);
    }
}
