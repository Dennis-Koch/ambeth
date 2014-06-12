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
        bool IsCreateAllowed(Object entity, params ISecurityScope[] securityScopes);

        bool IsUpdateAllowed(Object entity, params ISecurityScope[] securityScopes);

        bool IsDeleteAllowed(Object entity, params ISecurityScope[] securityScopes);

        bool IsReadAllowed(Object entity, params ISecurityScope[] securityScopes);

        bool IsExecutionAllowed(Object entity, params ISecurityScope[] securityScopes);

        IPrivilegeItem GetPrivilege(Object entity, params ISecurityScope[]  securityScopes);

	    IPrivilegeItem GetPrivilegeByObjRef(IObjRef objRef, params ISecurityScope[]  securityScopes);
        
        /// <summary>
        /// Result correlates by-index with the given objRefs
        /// </summary>
        /// <typeparam name="V"></typeparam>
        /// <param name="entities"></param>
        /// <param name="securityScopes"></param>
        /// <returns></returns>
	    IList<IPrivilegeItem> GetPrivileges<V>(IEnumerable<V> entities, params ISecurityScope[] securityScopes);

        /// <summary>
        /// Result correlates by-index with the given objRefs
        /// </summary>
        /// <typeparam name="V"></typeparam>
        /// <param name="objRefs"></param>
        /// <param name="securityScopes"></param>
        /// <returns></returns>
        IList<IPrivilegeItem> GetPrivilegesByObjRef<V>(IEnumerable<V> objRefs, params ISecurityScope[] securityScopes) where V : IObjRef;
    }
}
