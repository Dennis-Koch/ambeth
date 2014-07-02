using System;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Model;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Security.Transfer;
using De.Osthus.Ambeth.Privilege.Model;
using De.Osthus.Ambeth.Privilege.Transfer;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Security;
using System.Security;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Privilege.Model.Impl;

namespace De.Osthus.Ambeth.Privilege
{
    public class PrivilegeProvider : IPrivilegeProvider, IInitializingBean, IDataChangeListener
    {
        public class PrivilegeKey
        {
            public Type EntityType;

            public Object Id;

            public sbyte IdIndex;

            public String SecurityScope;

            public String userSID;

            public PrivilegeKey()
            {
                // Intended blank
            }

            public PrivilegeKey(Type entityType, sbyte IdIndex, Object id, String userSID)
            {
                this.EntityType = entityType;
                this.IdIndex = IdIndex;
                this.Id = id;
                this.userSID = userSID;
            }

            public override int GetHashCode()
            {
                if (SecurityScope == null)
                {
                    return EntityType.GetHashCode() ^ Id.GetHashCode() ^ userSID.GetHashCode();
                }
                else
                {
                    return EntityType.GetHashCode() ^ Id.GetHashCode() ^ userSID.GetHashCode() ^ SecurityScope.GetHashCode();
                }
            }

            public override bool Equals(object obj)
            {
                if (Object.ReferenceEquals(this, obj))
                {
                    return true;
                }
                if (!(obj is PrivilegeKey))
                {
                    return false;
                }
                PrivilegeKey other = (PrivilegeKey)obj;
                return Object.Equals(Id, other.Id)
                    && Object.Equals(EntityType, other.EntityType)
                    && IdIndex == other.IdIndex
                    && Object.Equals(userSID, other.userSID)
                    && Object.Equals(SecurityScope, other.SecurityScope);
            }

            public override String ToString()
            {
                return "PrivilegeKey: " + EntityType.FullName + "(" + IdIndex + "," + Id + ") SecurityScope: '" + SecurityScope + "',SID:" + userSID;
            }
        }

        public class PrivilegeKeyOfType
        {
            public Type entityType;

            public String securityScope;

            public String userSID;

            public PrivilegeKeyOfType()
            {
                // Intended blank
            }

            public PrivilegeKeyOfType(Type entityType, String userSID)
            {
                this.entityType = entityType;
                this.userSID = userSID;
            }

            public override int GetHashCode()
            {
                if (securityScope == null)
                {
                    return GetType().GetHashCode() ^ entityType.GetHashCode() ^ userSID.GetHashCode();
                }
                else
                {
                    return GetType().GetHashCode() ^ entityType.GetHashCode() ^ userSID.GetHashCode() ^ securityScope.GetHashCode();
                }
            }

            public override bool Equals(Object obj)
            {
                if (this == obj)
                {
                    return true;
                }
                if (!(obj is PrivilegeKeyOfType))
                {
                    return false;
                }
                PrivilegeKeyOfType other = (PrivilegeKeyOfType)obj;
                return Object.Equals(entityType, other.entityType) && Object.Equals(userSID, other.userSID)
                        && Object.Equals(securityScope, other.securityScope);
            }

            public override String ToString()
            {
                return "PrivilegeKeyOfType: " + entityType.FullName + " SecurityScope: '" + securityScope + "',SID:" + userSID;
            }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IInterningFeature InterningFeature { protected get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }

        [Autowired]
        public IPrivilegeService PrivilegeService { protected get; set; }

        [Autowired]
        public ISecurityScopeProvider SecurityScopeProvider { protected get; set; }

        protected readonly Object writeLock = new Object();

        protected readonly HashMap<Object, IPrivilege> privilegeCache = new HashMap<Object, IPrivilege>();

        public virtual void AfterPropertiesSet()
        {
            if (PrivilegeService == null && Log.DebugEnabled)
            {
                Log.Debug("Privilege Service could not be resolved - Privilege functionality deactivated");
            }
        }

        public bool IsCreateAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivilege(entity, securityScopes).CreateAllowed;
        }

        public bool IsUpdateAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivilege(entity, securityScopes).UpdateAllowed;
        }

        public bool IsDeleteAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivilege(entity, securityScopes).DeleteAllowed;
        }

        public bool IsReadAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivilege(entity, securityScopes).ReadAllowed;
        }

        public bool IsExecutionAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivilege(entity, securityScopes).ExecutionAllowed;
        }

        public IPrivilege GetPrivilege(Object entity, params ISecurityScope[] securityScopes)
        {
            IList<IObjRef> objRefs = ObjRefHelper.ExtractObjRefList(entity, null);
            IList<IPrivilege> result = GetPrivileges(objRefs, securityScopes);
            if (result.Count == 0)
            {
                return DenyAllPrivilege.INSTANCE;
            }
            return result[0];
        }

        public IPrivilege GetPrivilegeByObjRef(IObjRef objRef, params ISecurityScope[] securityScopes)
        {
            IList<IPrivilege> result = GetPrivilegesByObjRef(new List<IObjRef>(new IObjRef[] { objRef }), securityScopes);
            if (result.Count == 0)
            {
                return DenyAllPrivilege.INSTANCE;
            }
            return result[0];
        }

        public IList<IPrivilege> GetPrivileges<V>(IEnumerable<V> entities, params ISecurityScope[] securityScopes)
        {
            IList<IObjRef> objRefs = ObjRefHelper.ExtractObjRefList(entities, null);
            return GetPrivilegesByObjRef(objRefs, securityScopes);
        }

        public IList<IPrivilege> GetPrivilegesByObjRef<V>(IEnumerable<V> objRefs, params ISecurityScope[] securityScopes) where V : IObjRef
        {
            IAuthorization authorization = SecurityScopeProvider.Authorization;
            if (authorization == null)
            {
                throw new SecurityException("User must be authenticated to be able to check for privileges");
            }
            if (securityScopes.Length == 0)
            {
                throw new ArgumentException("No " + typeof(ISecurityScope).Name + " provided to check privileges against");
            }
            List<IObjRef> missingObjRefs = new List<IObjRef>();
            lock (writeLock)
            {
                IList<IPrivilege> result = CreateResult(objRefs, securityScopes, missingObjRefs, authorization, null);
                if (missingObjRefs.Count == 0)
                {
                    return result;
                }
            }
            String userSID = authorization.SID;
            IList<IPrivilegeOfService> privilegeResults = PrivilegeService.GetPrivileges(missingObjRefs.ToArray(), securityScopes);
            lock (writeLock)
            {
                HashMap<PrivilegeKey, IPrivilege> privilegeResultOfNewEntities = null;
                for (int a = 0, size = privilegeResults.Count; a < size; a++)
                {
                    IPrivilegeOfService privilegeResult = privilegeResults[a];
                    IObjRef reference = privilegeResult.Reference;

                    PrivilegeKey privilegeKey = new PrivilegeKey(reference.RealType, reference.IdNameIndex, reference.Id, userSID);
                    bool useCache = true;
                    if (privilegeKey.Id == null)
                    {
                        useCache = false;
                        privilegeKey.Id = reference;
                    }
                    privilegeKey.SecurityScope = InterningFeature.Intern(privilegeResult.SecurityScope.Name);

                    String[] propertyPrivilegeNames = privilegeResult.PropertyPrivilegeNames;
                    IPropertyPrivilegeOfService[] propertyPrivileges = privilegeResult.PropertyPrivileges;

                    String[] propertyNames;
                    IMap<String, IPropertyPrivilege> propertyPrivilegeMap;
                    if (propertyPrivileges == null || propertyPrivileges.Length == 0)
                    {
                        propertyNames = PrivilegeImpl.EMPTY_PROPERTY_NAMES;
                        propertyPrivilegeMap = EmptyMap<String, IPropertyPrivilege>.Empty();
                    }
                    else
                    {
                        propertyNames = propertyPrivilegeNames;
                        propertyPrivilegeMap = HashMap<String, IPropertyPrivilege>.Create(propertyPrivileges.Length);
                        for (int b = propertyPrivileges.Length; b-- > 0; )
                        {
                            IPropertyPrivilegeOfService propertyPrivilege = propertyPrivileges[b];
                            String propertyName = InterningFeature.Intern(propertyPrivilegeNames[b]);
                            propertyPrivilegeMap.Put(propertyName, PropertyPrivilegeImpl.CreateFrom(propertyPrivilege));
                        }
                    }
                    PrivilegeImpl pi = new PrivilegeImpl(privilegeResult.ReadAllowed, privilegeResult.CreateAllowed, privilegeResult.UpdateAllowed,
                            privilegeResult.DeleteAllowed, privilegeResult.ExecutionAllowed, propertyPrivilegeMap, propertyNames);
                    if (useCache)
                    {
                        privilegeCache.Put(privilegeKey, pi);
                    }
                    else
                    {
                        if (privilegeResultOfNewEntities == null)
                        {
                            privilegeResultOfNewEntities = new HashMap<PrivilegeKey, IPrivilege>();
                        }
                        privilegeResultOfNewEntities.Put(privilegeKey, pi);
                    }
                }
                return CreateResult(objRefs, securityScopes, null, authorization, privilegeResultOfNewEntities);
            }
        }

        public IPrivilege GetPrivilegeByType(Type entityType, params ISecurityScope[] securityScopes)
        {
            IList<IPrivilege> result = GetPrivilegesByType(new Type[] { entityType }, securityScopes);
            if (result.Count == 0)
            {
                return DenyAllPrivilege.INSTANCE;
            }
            return result[0];
        }

        public IList<IPrivilege> GetPrivilegesByType(IEnumerable<Type> entityTypes, params ISecurityScope[] securityScopes)
        {
            IAuthorization authorization = SecurityScopeProvider.Authorization;
            if (authorization == null)
            {
                throw new SecurityException("User must be authenticated to be able to check for privileges");
            }
            if (securityScopes.Length == 0)
            {
                throw new ArgumentException("No " + typeof(ISecurityScope).Name + " provided to check privileges against");
            }
            List<Type> missingEntityTypes = new List<Type>();
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
                IList<IPrivilege> result = CreateResultByType(entityTypes, securityScopes, missingEntityTypes, authorization);
                if (missingEntityTypes.Count == 0)
                {
                    return result;
                }
            }
            String userSID = authorization.SID;
            IList<IPrivilegeOfService> privilegeResults = PrivilegeService.GetPrivilegesOfTypes(missingEntityTypes.ToArray(), securityScopes);
            lock (writeLock)
            {
                for (int a = 0, size = privilegeResults.Count; a < size; a++)
                {
                    IPrivilegeOfService privilegeResult = privilegeResults[a];
                    IObjRef reference = privilegeResult.Reference;

                    PrivilegeKeyOfType privilegeKey = new PrivilegeKeyOfType(reference.RealType, userSID);
                    privilegeKey.securityScope = InterningFeature.Intern(privilegeResult.SecurityScope.Name);

                    String[] propertyPrivilegeNames = privilegeResult.PropertyPrivilegeNames;
                    IPropertyPrivilegeOfService[] propertyPrivileges = privilegeResult.PropertyPrivileges;

                    String[] propertyNames;
                    IMap<String, IPropertyPrivilege> propertyPrivilegeMap;
                    if (propertyPrivileges == null || propertyPrivileges.Length == 0)
                    {
                        propertyNames = PrivilegeImpl.EMPTY_PROPERTY_NAMES;
                        propertyPrivilegeMap = EmptyMap<String, IPropertyPrivilege>.Empty();
                    }
                    else
                    {
                        propertyNames = propertyPrivilegeNames;
                        propertyPrivilegeMap = HashMap<String, IPropertyPrivilege>.Create(propertyPrivileges.Length);
                        for (int b = propertyPrivileges.Length; b-- > 0; )
                        {
                            IPropertyPrivilegeOfService propertyPrivilegeResult = propertyPrivileges[b];
                            String propertyName = InterningFeature.Intern(propertyPrivilegeNames[b]);
                            propertyPrivilegeMap.Put(propertyName, PropertyPrivilegeImpl.CreateFrom(propertyPrivilegeResult));
                        }
                    }
                    PrivilegeImpl pi = new PrivilegeImpl(privilegeResult.ReadAllowed, privilegeResult.CreateAllowed, privilegeResult.UpdateAllowed,
                            privilegeResult.DeleteAllowed, privilegeResult.ExecutionAllowed, propertyPrivilegeMap, propertyNames);
                    privilegeCache.Put(privilegeKey, pi);
                }
                return CreateResultByType(entityTypes, securityScopes, null, authorization);
            }
        }

        protected IList<IPrivilege> CreateResult<V>(IEnumerable<V> objRefs, ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs,
                IAuthorization authorization, IMap<PrivilegeKey, IPrivilege> privilegeResultOfNewEntities) where V : IObjRef
        {
            PrivilegeKey privilegeKey = null;

            List<IPrivilege> result = new List<IPrivilege>();
            String userSID = authorization.SID;

            foreach (IObjRef objRef in objRefs)
            {
                if (objRef == null)
                {
                    result.Add(null);
                    continue;
                }
                if (privilegeKey == null)
                {
                    privilegeKey = new PrivilegeKey();
                }
                bool useCache = true;
                privilegeKey.EntityType = objRef.RealType;
                privilegeKey.IdIndex = objRef.IdNameIndex;
                privilegeKey.Id = objRef.Id;
                privilegeKey.userSID = userSID;
                if (privilegeKey.Id == null)
                {
                    useCache = false;
                    // use the ObjRef instance as the id
                    privilegeKey.Id = objRef;
                }

                IPrivilege mergedPrivilegeItem = null;
                for (int a = securityScopes.Length; a-- > 0; )
                {
                    privilegeKey.SecurityScope = securityScopes[a].Name;

                    IPrivilege existingPrivilegeItem = useCache ? privilegeCache.Get(privilegeKey)
                            : privilegeResultOfNewEntities != null ? privilegeResultOfNewEntities.Get(privilegeKey) : null;
                    if (existingPrivilegeItem == null)
                    {
                        mergedPrivilegeItem = null;
                        break;
                    }
                    if (mergedPrivilegeItem == null)
                    {
                        // Take first existing privilege as a start
                        mergedPrivilegeItem = existingPrivilegeItem;
                    }
                    else
                    {
                        // Merge all other existing privileges by boolean OR
                        throw new NotSupportedException("Not yet implemented");
                    }
                }
                if (mergedPrivilegeItem == null)
                {
                    if (missingObjRefs != null)
                    {
                        missingObjRefs.Add(objRef);
                    }
                    else
                    {
                        result.Add(DenyAllPrivilege.INSTANCE);
                    }
                    continue;
                }
                result.Add(mergedPrivilegeItem);
            }
            return result;
        }

        protected IList<IPrivilege> CreateResultByType(IEnumerable<Type> entityTypes, ISecurityScope[] securityScopes, IList<Type> missingEntityTypes,
            IAuthorization authorization)
        {
            PrivilegeKeyOfType privilegeKey = null;

            List<IPrivilege> result = new List<IPrivilege>();
            String userSID = authorization.SID;

            foreach (Type entityType in entityTypes)
            {
                if (entityType == null)
                {
                    result.Add(null);
                    continue;
                }
                if (privilegeKey == null)
                {
                    privilegeKey = new PrivilegeKeyOfType();
                }
                privilegeKey.entityType = entityType;
                privilegeKey.userSID = userSID;

                IPrivilege mergedPrivilegeItem = null;
                for (int a = securityScopes.Length; a-- > 0; )
                {
                    privilegeKey.securityScope = securityScopes[a].Name;

                    IPrivilege existingPrivilegeItem = privilegeCache.Get(privilegeKey);
                    if (existingPrivilegeItem == null)
                    {
                        mergedPrivilegeItem = null;
                        break;
                    }
                    if (mergedPrivilegeItem == null)
                    {
                        // Take first existing privilege as a start
                        mergedPrivilegeItem = existingPrivilegeItem;
                    }
                    else
                    {
                        // Merge all other existing privileges by boolean OR
                        throw new NotSupportedException("Not yet implemented");
                    }
                }
                if (mergedPrivilegeItem == null)
                {
                    if (missingEntityTypes != null)
                    {
                        missingEntityTypes.Add(entityType);
                    }
                    else
                    {
                        result.Add(DenyAllPrivilege.INSTANCE);
                    }
                    continue;
                }
                result.Add(mergedPrivilegeItem);
            }
            return result;
        }

        public void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            if (dataChange.IsEmpty)
            {
                return;
            }
            lock (writeLock)
            {
                privilegeCache.Clear();
            }
        }
    }
}