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

            public PrivilegeKey()
            {
                // Intended blank
            }

            public PrivilegeKey(Type entityType, sbyte IdIndex, Object id)
            {
                this.EntityType = entityType;
                this.IdIndex = IdIndex;
                this.Id = id;
            }

            public override int GetHashCode()
            {
                if (SecurityScope == null)
                {
                    return EntityType.GetHashCode() ^ Id.GetHashCode();
                }
                else
                {
                    return EntityType.GetHashCode() ^ Id.GetHashCode() ^ SecurityScope.GetHashCode();
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
                    && Object.Equals(SecurityScope, other.SecurityScope);
            }

            public override String ToString()
            {
                return "CacheKey: " + EntityType.FullName + "(" + IdIndex + "," + Id + ") SecurityScope: '" + SecurityScope + "'";
            }
        }

        [LogInstance]
		public ILogger Log { private get; set; }

        public IObjRefHelper OriHelper { protected get; set; }

        public IPrivilegeService PrivilegeService { protected get; set; }

        protected readonly IDictionary<PrivilegeKey, PrivilegeEnum[]> privilegeCache = new Dictionary<PrivilegeKey, PrivilegeEnum[]>();

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(OriHelper, "OriHelper");
            ParamChecker.AssertNotNull(PrivilegeService, "PrivilegeService");
        }

        public void BuildPrefetchConfig(Type entityType, IPrefetchConfig prefetchConfig)
        {
            // Intended blank
        }

        public bool IsCreateAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivileges(entity, securityScopes).IsCreateAllowed();
        }

        public bool IsUpdateAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivileges(entity, securityScopes).IsUpdateAllowed();
        }

        public bool IsDeleteAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivileges(entity, securityScopes).IsDeleteAllowed();
        }

        public bool IsReadAllowed(Object entity, params ISecurityScope[] securityScopes)
        {
            return GetPrivileges(entity, securityScopes).IsReadAllowed();
        }

        public IPrivilegeItem GetPrivileges(Object entity, params ISecurityScope[] securityScopes)
        {
            IList<IObjRef> objRefs = OriHelper.ExtractObjRefList(entity, null);
            IList<IPrivilegeItem> result = GetPrivileges(objRefs, securityScopes);
            if (result.Count == 0)
            {
                return new PrivilegeItem(new PrivilegeEnum[4]);
            }
            return result[0];
        }

        public IList<IPrivilegeItem> GetPrivileges(IList<IObjRef> objRefs, params ISecurityScope[] securityScopes)
        {
            List<IObjRef> missingObjRefs = new List<IObjRef>();
            lock (privilegeCache)
            {
                IList<IPrivilegeItem> result = CreateResult(objRefs, securityScopes, missingObjRefs);
                if (missingObjRefs.Count == 0)
                {
                    return result;
                }
            }
            IList<PrivilegeResult> privilegeResults = PrivilegeService.GetPrivileges(missingObjRefs.ToArray(), securityScopes);
            lock (privilegeCache)
            {
                foreach (PrivilegeResult privilegeResult in privilegeResults)
                {
                    IObjRef reference = privilegeResult.Reference;

                    PrivilegeKey privilegeKey = new PrivilegeKey(reference.RealType, reference.IdNameIndex, reference.Id);
                    privilegeKey.SecurityScope = privilegeResult.SecurityScope.Name;
                    
                    PrivilegeEnum[] privilegeEnums = privilegeResult.Privileges;

                    PrivilegeEnum[] indexedPrivilegeEnums = new PrivilegeEnum[4];
                    if (privilegeEnums != null)
                    {
                        foreach (PrivilegeEnum privilegeEnum in privilegeEnums)
                        {
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
                                default:
                                    throw new Exception(typeof(PrivilegeEnum).FullName + " not supported: " + privilegeEnum);
                            }
                        }
                    }
                    privilegeCache[privilegeKey] = indexedPrivilegeEnums;
                }
                return CreateResult(objRefs, securityScopes, null);
            }
        }

        protected IList<IPrivilegeItem> CreateResult(IList<IObjRef> objRefs, ISecurityScope[] securityScopes, IList<IObjRef> missingObjRefs)
        {
            PrivilegeKey privilegeKey = null;

            IList<IPrivilegeItem> result = new List<IPrivilegeItem>(objRefs.Count);

            foreach (IObjRef objRef in objRefs)
            {
                if (privilegeKey == null)
                {
                    privilegeKey = new PrivilegeKey();
                }
                privilegeKey.EntityType = objRef.RealType;
                privilegeKey.IdIndex = objRef.IdNameIndex;
                privilegeKey.Id = objRef.Id;
                
                PrivilegeEnum[] mergedPrivilegeValues = null;
                for (int a = securityScopes.Length; a-- > 0; )
                {
                    privilegeKey.SecurityScope = securityScopes[a].Name;

                    PrivilegeEnum[] existingPrivilegeValues = DictionaryExtension.ValueOrDefault(privilegeCache, privilegeKey);
                    if (existingPrivilegeValues == null)
                    {
                        mergedPrivilegeValues = null;
                        break;
                    }
                    if (mergedPrivilegeValues == null)
                    {
                        // Take first existing privilege as a start
                        mergedPrivilegeValues = new PrivilegeEnum[4];
                        existingPrivilegeValues.CopyTo(mergedPrivilegeValues, 0);
                    }
                    else
                    {
                        // Merge all other existing privileges by boolean OR
                        for (int b = mergedPrivilegeValues.Length; b-- > 0; )
                        {
                            PrivilegeEnum existingPrivilegeValue = existingPrivilegeValues[b];
                            if (!PrivilegeEnum.NONE.Equals(existingPrivilegeValue))
                            {
                                mergedPrivilegeValues[b] = existingPrivilegeValue;
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
                    continue;
                }
                privilegeKey.SecurityScope = null;

                result.Add(new PrivilegeItem(mergedPrivilegeValues));
                privilegeKey = null;
            }
            return result;
        }

        public void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            if (dataChange.IsEmpty)
            {
                return;
            }
            lock (privilegeCache)
            {
                privilegeCache.Clear();
            }
        }
    }
}