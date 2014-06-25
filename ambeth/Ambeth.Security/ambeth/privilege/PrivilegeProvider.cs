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

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }

        [Autowired]
        public IPrivilegeService PrivilegeService { protected get; set; }

        [Autowired]
        public ISecurityScopeProvider SecurityScopeProvider { protected get; set; }

        protected readonly HashMap<PrivilegeKey, PrivilegeEnum[]> privilegeCache = new HashMap<PrivilegeKey, PrivilegeEnum[]>();

        protected readonly Object writeLock = new Object();

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

        public IPrivilegeItem GetPrivilege(Object entity, params ISecurityScope[] securityScopes)
        {
            IList<IObjRef> objRefs = ObjRefHelper.ExtractObjRefList(entity, null);
            IList<IPrivilegeItem> result = GetPrivileges(objRefs, securityScopes);
            if (result.Count == 0)
            {
                return PrivilegeItem.DENY_ALL;
            }
            return result[0];
        }

        public IPrivilegeItem GetPrivilegeByObjRef(IObjRef objRef, params ISecurityScope[] securityScopes)
        {
            IList<IPrivilegeItem> result = GetPrivilegesByObjRef(new List<IObjRef>(new IObjRef[] { objRef }), securityScopes);
            if (result.Count == 0)
            {
                return PrivilegeItem.DENY_ALL;
            }
            return result[0];
        }

        public IList<IPrivilegeItem> GetPrivileges<V>(IEnumerable<V> entities, params ISecurityScope[] securityScopes)
        {
            IList<IObjRef> objRefs = ObjRefHelper.ExtractObjRefList(entities, null);
            return GetPrivilegesByObjRef(objRefs, securityScopes);
        }

        public IList<IPrivilegeItem> GetPrivilegesByObjRef<V>(IEnumerable<V> objRefs, params ISecurityScope[] securityScopes) where V : IObjRef
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
                IList<IPrivilegeItem> result = CreateResult(objRefs, securityScopes, missingObjRefs, authorization, null);
                if (missingObjRefs.Count == 0)
                {
                    return result;
                }
            }
            String userSID = authorization.SID;
            IList<PrivilegeResult> privilegeResults = PrivilegeService.GetPrivileges(missingObjRefs.ToArray(), securityScopes);
            lock (writeLock)
            {
                HashMap<PrivilegeKey, PrivilegeEnum[]> privilegeResultOfNewEntities = null;
                for (int a = 0, size = privilegeResults.Count; a < size; a++)
                {
                    PrivilegeResult privilegeResult = privilegeResults[a];
                    IObjRef reference = privilegeResult.Reference;

                    PrivilegeKey privilegeKey = new PrivilegeKey(reference.RealType, reference.IdNameIndex, reference.Id, userSID);
                    bool useCache = true;
				    if (privilegeKey.Id == null)
				    {
					    useCache = false;
					    privilegeKey.Id = reference;
				    }
                    privilegeKey.SecurityScope = privilegeResult.SecurityScope.Name;

                    PrivilegeEnum[] privilegeEnums = privilegeResult.Privileges;

                    PrivilegeEnum[] indexedPrivilegeEnums = new PrivilegeEnum[5];
                    if (privilegeEnums != null)
                    {
                        for (int b = privilegeEnums.Length; b-- > 0; )
                        {
                            PrivilegeEnum privilegeEnum = privilegeEnums[b];
                            switch (privilegeEnum)
                            {
                                case PrivilegeEnum.NONE:
                                    {
                                        break;
                                    }
                                case PrivilegeEnum.CREATE_ALLOWED:
                                    {
                                        indexedPrivilegeEnums[PrivilegeItem.CREATE_INDEX] = privilegeEnum;
                                        break;
                                    }
                                case PrivilegeEnum.UPDATE_ALLOWED:
                                    {
                                        indexedPrivilegeEnums[PrivilegeItem.UPDATE_INDEX] = privilegeEnum;
                                        break;
                                    }
                                case PrivilegeEnum.DELETE_ALLOWED:
                                    {
                                        indexedPrivilegeEnums[PrivilegeItem.DELETE_INDEX] = privilegeEnum;
                                        break;
                                    }
                                case PrivilegeEnum.READ_ALLOWED:
                                    {
                                        indexedPrivilegeEnums[PrivilegeItem.READ_INDEX] = privilegeEnum;
                                        break;
                                    }
                                case PrivilegeEnum.EXECUTE_ALLOWED:
                                    {
                                        indexedPrivilegeEnums[PrivilegeItem.EXECUTION_INDEX] = privilegeEnum;
                                        break;
                                    }
                                default:
                                    throw RuntimeExceptionUtil.CreateEnumNotSupportedException(privilegeEnum);
                            }
                        }
                    }
                    if (useCache)
				    {
					    privilegeCache.Put(privilegeKey, indexedPrivilegeEnums);
				    }
				    else
				    {
					    if (privilegeResultOfNewEntities == null)
					    {
						    privilegeResultOfNewEntities = new HashMap<PrivilegeKey, PrivilegeEnum[]>();
					    }
					    privilegeResultOfNewEntities.Put(privilegeKey, indexedPrivilegeEnums);
				    }
                }
                return CreateResult(objRefs, securityScopes, null, authorization, privilegeResultOfNewEntities);
            }
        }

        protected IList<IPrivilegeItem> CreateResult<V>(IEnumerable<V> objRefs, ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs,
                IAuthorization authorization, IMap<PrivilegeKey, PrivilegeEnum[]> privilegeResultOfNewEntities) where V : IObjRef
        {
            PrivilegeKey privilegeKey = null;

            List<IPrivilegeItem> result = new List<IPrivilegeItem>();
            String userSID = authorization.SID;

            foreach (IObjRef objRef in objRefs)
            {
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

                PrivilegeEnum[] mergedPrivilegeValues = null;
                for (int a = securityScopes.Length; a-- > 0; )
                {
                    privilegeKey.SecurityScope = securityScopes[a].Name;

                    PrivilegeEnum[] existingPrivilegeValues = useCache ? privilegeCache.Get(privilegeKey)
                            : privilegeResultOfNewEntities != null ? privilegeResultOfNewEntities.Get(privilegeKey) : null;
                    if (existingPrivilegeValues == null)
                    {
                        mergedPrivilegeValues = null;
                        break;
                    }
                    if (mergedPrivilegeValues == null)
                    {
                        // Take first existing privilege as a start
                        mergedPrivilegeValues = new PrivilegeEnum[existingPrivilegeValues.Length];
                        Array.Copy(existingPrivilegeValues, 0, mergedPrivilegeValues, 0, existingPrivilegeValues.Length);
                    }
                    else
                    {
                        // Merge all other existing privileges by boolean OR
                        for (int c = mergedPrivilegeValues.Length; c-- > 0; )
                        {
                            PrivilegeEnum existingPrivilegeValue = existingPrivilegeValues[c];
                            if (!PrivilegeEnum.NONE.Equals(existingPrivilegeValue))
                            {
                                mergedPrivilegeValues[c] = existingPrivilegeValue;
                            }
                        }
                    }
                }
                if (mergedPrivilegeValues == null)
                {
                    if (missingObjRefs != null)
                    {
                        missingObjRefs.Add(objRef);
                    }
                    else
                    {
                        result.Add(PrivilegeItem.DENY_ALL);
                    }
                    continue;
                }
                result.Add(new PrivilegeItem(mergedPrivilegeValues));
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