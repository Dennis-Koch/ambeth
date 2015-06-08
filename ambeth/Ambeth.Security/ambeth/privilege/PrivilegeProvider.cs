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
using De.Osthus.Ambeth.Privilege.Factory;

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

		public static readonly String m_HandleClearAllCaches = "HandleClearAllCaches";

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IEntityPrivilegeFactoryProvider EntityPrivilegeFactoryProvider { protected get; set; }

        [Autowired]
        public IEntityTypePrivilegeFactoryProvider EntityTypePrivilegeFactoryProvider { protected get; set; }

        [Autowired]
        public IInterningFeature InterningFeature { protected get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }

        [Autowired(Optional = true)]
        public IPrivilegeService PrivilegeService { protected get; set; }

        [Autowired]
        public ISecurityContextHolder SecurityContextHolder { protected get; set; }

        [Autowired]
        public ISecurityScopeProvider SecurityScopeProvider { protected get; set; }

        protected readonly Object writeLock = new Object();

        protected readonly LinkedHashMap<PrivilegeKey, IPrivilege> privilegeCache = new LinkedHashMap<PrivilegeKey, IPrivilege>();

        protected readonly Tuple3KeyHashMap<Type, String, String, ITypePrivilege> entityTypePrivilegeCache = new Tuple3KeyHashMap<Type, String, String, ITypePrivilege>();

        public virtual void AfterPropertiesSet()
        {
            if (PrivilegeService == null && Log.DebugEnabled)
            {
                Log.Debug("Privilege Service could not be resolved - Privilege functionality deactivated");
            }
        }

        public IPrivilege GetPrivilege(Object entity, params ISecurityScope[] securityScopes)
        {
            IList<IObjRef> objRefs = ObjRefHelper.ExtractObjRefList(entity, null);
            IPrivilegeResult result = GetPrivileges(objRefs, securityScopes);
			return result.GetPrivileges()[0];
        }

        public IPrivilege GetPrivilegeByObjRef(IObjRef objRef, params ISecurityScope[] securityScopes)
        {
			IPrivilegeResult result = GetPrivilegesByObjRef(new List<IObjRef>(new IObjRef[] { objRef }), securityScopes);
			return result.GetPrivileges()[0];
        }

		public IPrivilegeResult GetPrivileges<V>(IList<V> entities, params ISecurityScope[] securityScopes)
        {
            IList<IObjRef> objRefs = ObjRefHelper.ExtractObjRefList(entities, null);
            return GetPrivilegesByObjRef(objRefs, securityScopes);
        }

		public IPrivilegeResult GetPrivilegesByObjRef<V>(IList<V> objRefs, params ISecurityScope[] securityScopes) where V : IObjRef
        {
            ISecurityContext context = SecurityContextHolder.Context;
            IAuthorization authorization = context != null ? context.Authorization : null;
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
				IPrivilegeResult result = CreateResult(objRefs, securityScopes, missingObjRefs, authorization, null);
                if (missingObjRefs.Count == 0)
                {
                    return result;
                }
            }
            if (PrivilegeService == null)
		    {
			    throw new SecurityException("No bean of type " + typeof(IPrivilegeService).FullName
					    + " could be injected. Privilege functionality is deactivated. The current operation is not supported");
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

                    IPrivilege privilege = CreatePrivilegeFromServiceResult(reference, privilegeResult);
                    if (useCache)
                    {
                        privilegeCache.Put(privilegeKey, privilege);
                    }
                    else
                    {
                        if (privilegeResultOfNewEntities == null)
                        {
                            privilegeResultOfNewEntities = new HashMap<PrivilegeKey, IPrivilege>();
                        }
                        privilegeResultOfNewEntities.Put(privilegeKey, privilege);
                    }
                }
                return CreateResult(objRefs, securityScopes, null, authorization, privilegeResultOfNewEntities);
            }
        }

        protected IPrivilege CreatePrivilegeFromServiceResult(IObjRef objRef, IPrivilegeOfService privilegeOfService)
	    {
		    IPropertyPrivilegeOfService[] propertyPrivilegesOfService = privilegeOfService.PropertyPrivileges;

            if (propertyPrivilegesOfService == null || propertyPrivilegesOfService.Length == 0)
            {
                return SimplePrivilegeImpl.CreateFrom(privilegeOfService);
            }
		    String[] propertyPrivilegeNames = privilegeOfService.PropertyPrivilegeNames;
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objRef.RealType);
		    IPropertyPrivilege[] primitivePropertyPrivileges = new IPropertyPrivilege[metaData.PrimitiveMembers.Length];
            IPropertyPrivilege[] relationPropertyPrivileges = new IPropertyPrivilege[metaData.RelationMembers.Length];
            IPropertyPrivilege defaultPropertyPrivilege = PropertyPrivilegeImpl.CreateFrom(privilegeOfService);
            Arrays.Fill(primitivePropertyPrivileges, defaultPropertyPrivilege);
            Arrays.Fill(relationPropertyPrivileges, defaultPropertyPrivilege);
		    for (int b = propertyPrivilegesOfService.Length; b-- > 0;)
		    {
			    IPropertyPrivilegeOfService propertyPrivilegeOfService = propertyPrivilegesOfService[b];
			    String propertyName = InterningFeature.Intern(propertyPrivilegeNames[b]);
			    IPropertyPrivilege propertyPrivilege = PropertyPrivilegeImpl.Create(propertyPrivilegeOfService.CreateAllowed,
					    propertyPrivilegeOfService.ReadAllowed, propertyPrivilegeOfService.UpdateAllowed, propertyPrivilegeOfService.DeleteAllowed);
			    if (metaData.IsRelationMember(propertyName))
			    {
				    relationPropertyPrivileges[metaData.GetIndexByRelationName(propertyName)] = propertyPrivilege;
			    }
			    if (metaData.IsPrimitiveMember(propertyName))
			    {
				    primitivePropertyPrivileges[metaData.GetIndexByPrimitiveName(propertyName)] = propertyPrivilege;
			    }
		    }
		    return EntityPrivilegeFactoryProvider.GetEntityPrivilegeFactory(metaData.EntityType, privilegeOfService.CreateAllowed,
				    privilegeOfService.ReadAllowed, privilegeOfService.UpdateAllowed, privilegeOfService.DeleteAllowed,
				    privilegeOfService.ExecuteAllowed).CreatePrivilege(privilegeOfService.CreateAllowed, privilegeOfService.ReadAllowed,
				    privilegeOfService.UpdateAllowed, privilegeOfService.DeleteAllowed, privilegeOfService.ExecuteAllowed, primitivePropertyPrivileges,
				    relationPropertyPrivileges);
	    }

	    protected ITypePrivilege CreateTypePrivilegeFromServiceResult(Type entityType, ITypePrivilegeOfService privilegeOfService)
	    {
		    ITypePropertyPrivilegeOfService[] propertyPrivilegesOfService = privilegeOfService.PropertyPrivileges;

		    ITypePropertyPrivilege defaultPropertyPrivilege = TypePropertyPrivilegeImpl.CreateFrom(privilegeOfService);
		    if (propertyPrivilegesOfService == null || propertyPrivilegesOfService.Length == 0)
		    {
			    return new SimpleTypePrivilegeImpl(privilegeOfService.CreateAllowed, privilegeOfService.ReadAllowed, privilegeOfService.UpdateAllowed,
					    privilegeOfService.DeleteAllowed, privilegeOfService.ExecuteAllowed, defaultPropertyPrivilege);
		    }
            String[] propertyPrivilegeNames = privilegeOfService.PropertyPrivilegeNames;
		    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
            ITypePropertyPrivilege[] primitivePropertyPrivileges = new ITypePropertyPrivilege[metaData.PrimitiveMembers.Length];
		    ITypePropertyPrivilege[] relationPropertyPrivileges = new ITypePropertyPrivilege[metaData.RelationMembers.Length];
            for (int a = primitivePropertyPrivileges.Length; a-- > 0; )
            {
                primitivePropertyPrivileges[a] = defaultPropertyPrivilege;
            }
            for (int a = relationPropertyPrivileges.Length; a-- > 0; )
            {
                relationPropertyPrivileges[a] = defaultPropertyPrivilege;
            }
		    for (int b = propertyPrivilegesOfService.Length; b-- > 0;)
		    {
			    ITypePropertyPrivilegeOfService propertyPrivilegeOfService = propertyPrivilegesOfService[b];
                String propertyName = InterningFeature.Intern(propertyPrivilegeNames[b]);
			    ITypePropertyPrivilege propertyPrivilege;
			    if (propertyPrivilegeOfService != null)
			    {
				    propertyPrivilege = TypePropertyPrivilegeImpl.Create(propertyPrivilegeOfService.CreateAllowed, propertyPrivilegeOfService.ReadAllowed,
						    propertyPrivilegeOfService.UpdateAllowed, propertyPrivilegeOfService.DeleteAllowed);
			    }
			    else
			    {
                    propertyPrivilege = TypePropertyPrivilegeImpl.Create(null, null, null, null);
			    }
			    if (metaData.IsRelationMember(propertyName))
			    {
				    relationPropertyPrivileges[metaData.GetIndexByRelationName(propertyName)] = propertyPrivilege;
			    }
			    if (metaData.IsPrimitiveMember(propertyName))
			    {
				    primitivePropertyPrivileges[metaData.GetIndexByPrimitiveName(propertyName)] = propertyPrivilege;
			    }
		    }
		    return EntityTypePrivilegeFactoryProvider.GetEntityTypePrivilegeFactory(metaData.EntityType, privilegeOfService.CreateAllowed,
				    privilegeOfService.ReadAllowed, privilegeOfService.UpdateAllowed, privilegeOfService.DeleteAllowed,
				    privilegeOfService.ExecuteAllowed).CreatePrivilege(privilegeOfService.CreateAllowed, privilegeOfService.ReadAllowed,
				    privilegeOfService.UpdateAllowed, privilegeOfService.DeleteAllowed, privilegeOfService.ExecuteAllowed, primitivePropertyPrivileges,
				    relationPropertyPrivileges);
	    }

        public ITypePrivilege GetPrivilegeByType(Type entityType, params ISecurityScope[] securityScopes)
        {
			ITypePrivilegeResult result = GetPrivilegesByType(new Type[] { entityType }, securityScopes);
			return result.GetTypePrivileges()[0];
        }

		public ITypePrivilegeResult GetPrivilegesByType(IList<Type> entityTypes, params ISecurityScope[] securityScopes)
        {
            ISecurityContext context = SecurityContextHolder.Context;
            IAuthorization authorization = context != null ? context.Authorization : null;
            if (authorization == null)
            {
                throw new SecurityException("User must be authorized to be able to check for privileges");
            }
            if (securityScopes.Length == 0)
            {
                throw new ArgumentException("No " + typeof(ISecurityScope).Name + " provided to check privileges against");
            }
            List<Type> missingEntityTypes = new List<Type>();
            Object writeLock = this.writeLock;
            lock (writeLock)
            {
				ITypePrivilegeResult result = CreateResultByType(entityTypes, securityScopes, missingEntityTypes, authorization);
                if (missingEntityTypes.Count == 0)
                {
                    return result;
                }
            }
            if (PrivilegeService == null)
            {
                throw new SecurityException("No bean of type " + typeof(IPrivilegeService).FullName
                        + " could be injected. Privilege functionality is deactivated. The current operation is not supported");
            }
            String userSID = authorization.SID;
            IList<ITypePrivilegeOfService> privilegeResults = PrivilegeService.GetPrivilegesOfTypes(missingEntityTypes.ToArray(), securityScopes);
            lock (writeLock)
            {
                for (int a = 0, size = privilegeResults.Count; a < size; a++)
                {
                    ITypePrivilegeOfService privilegeResult = privilegeResults[a];
                    Type entityType = privilegeResult.EntityType;

                    String securityScope = InterningFeature.Intern(privilegeResult.SecurityScope.Name);

                    ITypePrivilege pi = CreateTypePrivilegeFromServiceResult(entityType, privilegeResult);
                    entityTypePrivilegeCache.Put(entityType, securityScope, userSID, pi);
                }
                return CreateResultByType(entityTypes, securityScopes, null, authorization);
            }
        }

		protected IPrivilegeResult CreateResult<V>(IList<V> objRefs, ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs,
                IAuthorization authorization, IMap<PrivilegeKey, IPrivilege> privilegeResultOfNewEntities) where V : IObjRef
        {
            PrivilegeKey privilegeKey = null;

            IPrivilege[] result = new IPrivilege[objRefs.Count];
            String userSID = authorization.SID;

			for (int index = objRefs.Count; index-- > 0; )
			{
				IObjRef objRef = objRefs[index];
				if (objRef == null)
				{
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
						continue;
                    }
					mergedPrivilegeItem = DenyAllPrivilege.INSTANCE;
                }
				result[index] = mergedPrivilegeItem;
            }
            return new PrivilegeResult(authorization.SID, result);
        }

        protected ITypePrivilegeResult CreateResultByType(IList<Type> entityTypes, ISecurityScope[] securityScopes, IList<Type> missingEntityTypes,
            IAuthorization authorization)
        {
			ITypePrivilege[] result = new ITypePrivilege[entityTypes.Count];
            String userSID = authorization.SID;

            for (int index = entityTypes.Count; index-- > 0;)
			{
				Type entityType = entityTypes[index];
				if (entityType == null)
				{
					continue;
				}
                ITypePrivilege mergedTypePrivilege = null;
                for (int a = securityScopes.Length; a-- > 0; )
                {
                    ITypePrivilege existingTypePrivilege = entityTypePrivilegeCache.Get(entityType, securityScopes[a].Name, userSID);
                    if (existingTypePrivilege == null)
                    {
                        mergedTypePrivilege = null;
                        break;
                    }
                    if (mergedTypePrivilege == null)
                    {
                        // Take first existing privilege as a start
                        mergedTypePrivilege = existingTypePrivilege;
                    }
                    else
                    {
                        // Merge all other existing privileges by boolean OR
                        throw new NotSupportedException("Not yet implemented");
                    }
                }
                if (mergedTypePrivilege == null)
                {
                    if (missingEntityTypes != null)
                    {
                        missingEntityTypes.Add(entityType);
						continue;
                    }
					mergedTypePrivilege = SkipAllTypePrivilege.INSTANCE;
                }
                result[index] = mergedTypePrivilege;
            }
			return new TypePrivilegeResult(authorization.SID, result);
        }

        public void HandleClearAllCaches(ClearAllCachesEvent evnt)
        {
            lock (writeLock)
            {
                privilegeCache.Clear();
                entityTypePrivilegeCache.Clear();
            }
        }

        public void DataChanged(IDataChange dataChange, DateTime dispatchTime, long sequenceId)
        {
            if (dataChange.IsEmpty)
            {
                return;
            }
            HandleClearAllCaches(null);
        }
    }
}