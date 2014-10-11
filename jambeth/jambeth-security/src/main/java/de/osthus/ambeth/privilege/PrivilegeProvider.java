package de.osthus.ambeth.privilege;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.Tuple3KeyHashMap;
import de.osthus.ambeth.datachange.IDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.factory.IEntityPrivilegeFactoryProvider;
import de.osthus.ambeth.privilege.factory.IEntityTypePrivilegeFactoryProvider;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.DenyAllPrivilege;
import de.osthus.ambeth.privilege.model.impl.PropertyPrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.SimplePrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.SimpleTypePrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.SkipAllTypePrivilege;
import de.osthus.ambeth.privilege.model.impl.TypePropertyPrivilegeImpl;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.IPropertyPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.ITypePrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.ITypePropertyPrivilegeOfService;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.service.IPrivilegeService;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IInterningFeature;

public class PrivilegeProvider implements IPrivilegeProviderIntern, IInitializingBean, IDataChangeListener
{
	public static class PrivilegeKey
	{
		public Class<?> entityType;

		public Object id;

		public byte idIndex;

		public String securityScope;

		public String userSID;

		public PrivilegeKey()
		{
			// Intended blank
		}

		public PrivilegeKey(Class<?> entityType, byte IdIndex, Object id, String userSID)
		{
			this.entityType = entityType;
			idIndex = IdIndex;
			this.id = id;
			this.userSID = userSID;
		}

		@Override
		public int hashCode()
		{
			if (securityScope == null)
			{
				return getClass().hashCode() ^ entityType.hashCode() ^ id.hashCode() ^ userSID.hashCode();
			}
			else
			{
				return getClass().hashCode() ^ entityType.hashCode() ^ id.hashCode() ^ userSID.hashCode() ^ securityScope.hashCode();
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (!(obj instanceof PrivilegeKey))
			{
				return false;
			}
			PrivilegeKey other = (PrivilegeKey) obj;
			return EqualsUtil.equals(id, other.id) && EqualsUtil.equals(entityType, other.entityType) && idIndex == other.idIndex
					&& EqualsUtil.equals(userSID, other.userSID) && EqualsUtil.equals(securityScope, other.securityScope);
		}

		@Override
		public String toString()
		{
			return "PrivilegeKey: " + entityType.getName() + "(" + idIndex + "," + id + ") SecurityScope: '" + securityScope + "',SID:" + userSID;
		}
	}

	@LogInstance
	protected ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEntityPrivilegeFactoryProvider entityPrivilegeFactoryProvider;

	@Autowired
	protected IEntityTypePrivilegeFactoryProvider entityTypePrivilegeFactoryProvider;

	@Autowired
	protected IInterningFeature interningFeature;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired(optional = true)
	protected IPrivilegeService privilegeService;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	protected final Lock writeLock = new ReentrantLock();

	protected final HashMap<PrivilegeKey, IPrivilege> privilegeCache = new HashMap<PrivilegeKey, IPrivilege>();

	protected final Tuple3KeyHashMap<Class<?>, String, String, ITypePrivilege> entityTypePrivilegeCache = new Tuple3KeyHashMap<Class<?>, String, String, ITypePrivilege>();

	@Override
	public void afterPropertiesSet()
	{
		if (privilegeService == null && log.isDebugEnabled())
		{
			log.debug("Privilege Service could not be resolved - Privilege functionality deactivated");
		}
	}

	@Override
	public IPrivilegeCache createPrivilegeCache()
	{
		return beanContext.registerAnonymousBean(PrivilegeCache.class).finish();
	}

	@Override
	public IPrivilege getPrivilege(Object entity)
	{
		return getPrivilege(entity, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilege getPrivilege(Object entity, ISecurityScope[] securityScopes)
	{
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entity, null);
		IList<IPrivilege> result = getPrivileges(objRefs, securityScopes);
		if (result.size() == 0)
		{
			return DenyAllPrivilege.INSTANCE;
		}
		return result.get(0);
	}

	@Override
	public IPrivilege getPrivilegeByObjRef(IObjRef objRef)
	{
		return getPrivilegeByObjRef(objRef, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilege getPrivilegeByObjRef(ObjRef objRef, IPrivilegeCache privilegeCache)
	{
		return getPrivilegeByObjRef(objRef);
	}

	@Override
	public IPrivilege getPrivilegeByObjRef(IObjRef objRef, ISecurityScope[] securityScopes)
	{
		IList<IPrivilege> result = getPrivilegesByObjRef(new ArrayList<IObjRef>(new IObjRef[] { objRef }), securityScopes);
		if (result.size() == 0)
		{
			return DenyAllPrivilege.INSTANCE;
		}
		return result.get(0);
	}

	@Override
	public IList<IPrivilege> getPrivileges(Collection<?> entities)
	{
		return getPrivileges(entities, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IList<IPrivilege> getPrivileges(Collection<?> entities, ISecurityScope[] securityScopes)
	{
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entities, null);
		return getPrivilegesByObjRef(objRefs, securityScopes);
	}

	@Override
	public IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs)
	{
		return getPrivilegesByObjRef(objRefs, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, IPrivilegeCache privilegeCache)
	{
		return getPrivilegesByObjRef(objRefs);
	}

	@Override
	public IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, ISecurityScope[] securityScopes)
	{
		IAuthorization authorization = securityContextHolder.getCreateContext().getAuthorization();
		if (authorization == null)
		{
			throw new SecurityException("User must be authorized to be able to check for privileges");
		}
		if (securityScopes.length == 0)
		{
			throw new IllegalArgumentException("No " + ISecurityScope.class.getSimpleName() + " provided to check privileges against");
		}
		ArrayList<IObjRef> missingObjRefs = new ArrayList<IObjRef>();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			IList<IPrivilege> result = createResult(objRefs, securityScopes, missingObjRefs, authorization, null);
			if (missingObjRefs.size() == 0)
			{
				return result;
			}
		}
		finally
		{
			writeLock.unlock();
		}
		if (privilegeService == null)
		{
			throw new SecurityException("No bean of type " + IPrivilegeService.class.getName()
					+ " could be injected. Privilege functionality is deactivated. The current operation is not supported");
		}
		String userSID = authorization.getSID();
		List<IPrivilegeOfService> privilegeResults = privilegeService.getPrivileges(missingObjRefs.toArray(IObjRef.class), securityScopes);
		writeLock.lock();
		try
		{
			HashMap<PrivilegeKey, IPrivilege> privilegeResultOfNewEntities = null;
			for (int a = 0, size = privilegeResults.size(); a < size; a++)
			{
				IPrivilegeOfService privilegeResult = privilegeResults.get(a);
				IObjRef reference = privilegeResult.getReference();

				PrivilegeKey privilegeKey = new PrivilegeKey(reference.getRealType(), reference.getIdNameIndex(), reference.getId(), userSID);
				boolean useCache = true;
				if (privilegeKey.id == null)
				{
					useCache = false;
					privilegeKey.id = reference;
				}
				privilegeKey.securityScope = interningFeature.intern(privilegeResult.getSecurityScope().getName());

				IPrivilege privilege = createPrivilegeFromServiceResult(reference, privilegeResult);
				if (useCache)
				{
					privilegeCache.put(privilegeKey, privilege);
				}
				else
				{
					if (privilegeResultOfNewEntities == null)
					{
						privilegeResultOfNewEntities = new HashMap<PrivilegeKey, IPrivilege>();
					}
					privilegeResultOfNewEntities.put(privilegeKey, privilege);
				}
			}
			return createResult(objRefs, securityScopes, null, authorization, privilegeResultOfNewEntities);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected IPrivilege createPrivilegeFromServiceResult(IObjRef objRef, IPrivilegeOfService privilegeOfService)
	{
		IPropertyPrivilegeOfService[] propertyPrivilegesOfService = privilegeOfService.getPropertyPrivileges();

		IPropertyPrivilege defaultPropertyPrivilege = PropertyPrivilegeImpl.createFrom(privilegeOfService);
		if (propertyPrivilegesOfService == null || propertyPrivilegesOfService.length == 0)
		{
			return new SimplePrivilegeImpl(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(),
					privilegeOfService.isDeleteAllowed(), privilegeOfService.isExecuteAllowed(), defaultPropertyPrivilege);
		}
		String[] propertyPrivilegeNames = privilegeOfService.getPropertyPrivilegeNames();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
		IPropertyPrivilege[] primitivePropertyPrivileges = new IPropertyPrivilege[metaData.getPrimitiveMembers().length];
		IPropertyPrivilege[] relationPropertyPrivileges = new IPropertyPrivilege[metaData.getRelationMembers().length];
		Arrays.fill(primitivePropertyPrivileges, defaultPropertyPrivilege);
		Arrays.fill(relationPropertyPrivileges, defaultPropertyPrivilege);
		for (int b = propertyPrivilegesOfService.length; b-- > 0;)
		{
			IPropertyPrivilegeOfService propertyPrivilegeOfService = propertyPrivilegesOfService[b];
			String propertyName = interningFeature.intern(propertyPrivilegeNames[b]);
			IPropertyPrivilege propertyPrivilege = PropertyPrivilegeImpl.create(propertyPrivilegeOfService.isCreateAllowed(),
					propertyPrivilegeOfService.isReadAllowed(), propertyPrivilegeOfService.isUpdateAllowed(), propertyPrivilegeOfService.isDeleteAllowed());
			if (metaData.isRelationMember(propertyName))
			{
				relationPropertyPrivileges[metaData.getIndexByRelationName(propertyName)] = propertyPrivilege;
			}
			if (metaData.isPrimitiveMember(propertyName))
			{
				primitivePropertyPrivileges[metaData.getIndexByPrimitiveName(propertyName)] = propertyPrivilege;
			}
		}
		return entityPrivilegeFactoryProvider.getEntityPrivilegeFactory(metaData.getEntityType(), privilegeOfService.isCreateAllowed(),
				privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed(),
				privilegeOfService.isExecuteAllowed()).createPrivilege(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(),
				privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed(), privilegeOfService.isExecuteAllowed(), primitivePropertyPrivileges,
				relationPropertyPrivileges);
	}

	protected ITypePrivilege createTypePrivilegeFromServiceResult(Class<?> entityType, ITypePrivilegeOfService privilegeOfService)
	{
		ITypePropertyPrivilegeOfService[] propertyPrivilegesOfService = privilegeOfService.getPropertyPrivileges();

		ITypePropertyPrivilege defaultPropertyPrivilege = TypePropertyPrivilegeImpl.createFrom(privilegeOfService);
		if (propertyPrivilegesOfService == null || propertyPrivilegesOfService.length == 0)
		{
			return new SimpleTypePrivilegeImpl(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(),
					privilegeOfService.isDeleteAllowed(), privilegeOfService.isExecuteAllowed(), defaultPropertyPrivilege);
		}
		String[] propertyPrivilegeNames = privilegeOfService.getPropertyPrivilegeNames();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		ITypePropertyPrivilege[] primitivePropertyPrivileges = new ITypePropertyPrivilege[metaData.getPrimitiveMembers().length];
		ITypePropertyPrivilege[] relationPropertyPrivileges = new ITypePropertyPrivilege[metaData.getRelationMembers().length];
		Arrays.fill(primitivePropertyPrivileges, defaultPropertyPrivilege);
		Arrays.fill(relationPropertyPrivileges, defaultPropertyPrivilege);
		for (int b = propertyPrivilegesOfService.length; b-- > 0;)
		{
			ITypePropertyPrivilegeOfService propertyPrivilegeOfService = propertyPrivilegesOfService[b];
			String propertyName = interningFeature.intern(propertyPrivilegeNames[b]);
			ITypePropertyPrivilege propertyPrivilege;
			if (propertyPrivilegeOfService != null)
			{
				propertyPrivilege = TypePropertyPrivilegeImpl.create(propertyPrivilegeOfService.isCreateAllowed(), propertyPrivilegeOfService.isReadAllowed(),
						propertyPrivilegeOfService.isUpdateAllowed(), propertyPrivilegeOfService.isDeleteAllowed());
			}
			else
			{
				propertyPrivilege = TypePropertyPrivilegeImpl.create(null, null, null, null);
			}
			if (metaData.isRelationMember(propertyName))
			{
				relationPropertyPrivileges[metaData.getIndexByRelationName(propertyName)] = propertyPrivilege;
			}
			if (metaData.isPrimitiveMember(propertyName))
			{
				primitivePropertyPrivileges[metaData.getIndexByPrimitiveName(propertyName)] = propertyPrivilege;
			}
		}
		return entityTypePrivilegeFactoryProvider.getEntityTypePrivilegeFactory(metaData.getEntityType(), privilegeOfService.isCreateAllowed(),
				privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed(),
				privilegeOfService.isExecuteAllowed()).createPrivilege(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(),
				privilegeOfService.isUpdateAllowed(), privilegeOfService.isDeleteAllowed(), privilegeOfService.isExecuteAllowed(), primitivePropertyPrivileges,
				relationPropertyPrivileges);
	}

	@Override
	public ITypePrivilege getPrivilegeByType(Class<?> entityType)
	{
		return getPrivilegeByType(entityType, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public ITypePrivilege getPrivilegeByType(Class<?> entityType, ISecurityScope[] securityScopes)
	{
		IList<ITypePrivilege> result = getPrivilegesByType(new ArrayList<Class<?>>(new Class<?>[] { entityType }), securityScopes);
		if (result.size() == 0)
		{
			return SkipAllTypePrivilege.INSTANCE;
		}
		return result.get(0);
	}

	@Override
	public IList<ITypePrivilege> getPrivilegesByType(Collection<Class<?>> entityTypes)
	{
		return getPrivilegesByType(entityTypes, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IList<ITypePrivilege> getPrivilegesByType(Collection<Class<?>> entityTypes, ISecurityScope[] securityScopes)
	{
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (authorization == null)
		{
			throw new SecurityException("User must be authorized to be able to check for privileges");
		}
		if (securityScopes.length == 0)
		{
			throw new IllegalArgumentException("No " + ISecurityScope.class.getSimpleName() + " provided to check privileges against");
		}
		ArrayList<Class<?>> missingEntityTypes = new ArrayList<Class<?>>();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			IList<ITypePrivilege> result = createResultByType(entityTypes, securityScopes, missingEntityTypes, authorization);
			if (missingEntityTypes.size() == 0)
			{
				return result;
			}
		}
		finally
		{
			writeLock.unlock();
		}
		if (privilegeService == null)
		{
			throw new SecurityException("No bean of type " + IPrivilegeService.class.getName()
					+ " could be injected. Privilege functionality is deactivated. The current operation is not supported");
		}
		String userSID = authorization.getSID();
		List<ITypePrivilegeOfService> privilegeResults = privilegeService.getPrivilegesOfTypes(missingEntityTypes.toArray(Class.class), securityScopes);
		writeLock.lock();
		try
		{
			for (int a = 0, size = privilegeResults.size(); a < size; a++)
			{
				ITypePrivilegeOfService privilegeResult = privilegeResults.get(a);
				Class<?> entityType = privilegeResult.getEntityType();

				String securityScope = interningFeature.intern(privilegeResult.getSecurityScope().getName());

				ITypePrivilege pi = createTypePrivilegeFromServiceResult(entityType, privilegeResult);
				entityTypePrivilegeCache.put(entityType, securityScope, userSID, pi);
			}
			return createResultByType(entityTypes, securityScopes, null, authorization);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected IList<IPrivilege> createResult(Collection<? extends IObjRef> objRefs, ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs,
			IAuthorization authorization, IMap<PrivilegeKey, IPrivilege> privilegeResultOfNewEntities)
	{
		PrivilegeKey privilegeKey = null;

		ArrayList<IPrivilege> result = new ArrayList<IPrivilege>(objRefs.size());
		String userSID = authorization.getSID();

		for (IObjRef objRef : objRefs)
		{
			if (objRef == null)
			{
				result.add(null);
				continue;
			}
			if (privilegeKey == null)
			{
				privilegeKey = new PrivilegeKey();
			}
			boolean useCache = true;
			privilegeKey.entityType = objRef.getRealType();
			privilegeKey.idIndex = objRef.getIdNameIndex();
			privilegeKey.id = objRef.getId();
			privilegeKey.userSID = userSID;
			if (privilegeKey.id == null)
			{
				useCache = false;
				// use the ObjRef instance as the id
				privilegeKey.id = objRef;
			}

			IPrivilege mergedPrivilegeItem = null;
			for (int a = securityScopes.length; a-- > 0;)
			{
				privilegeKey.securityScope = securityScopes[a].getName();

				IPrivilege existingPrivilegeItem = useCache ? privilegeCache.get(privilegeKey)
						: privilegeResultOfNewEntities != null ? privilegeResultOfNewEntities.get(privilegeKey) : null;
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
					throw new UnsupportedOperationException("Not yet implemented");
				}
			}
			if (mergedPrivilegeItem == null)
			{
				if (missingObjRefs != null)
				{
					missingObjRefs.add(objRef);
				}
				else
				{
					result.add(DenyAllPrivilege.INSTANCE);
				}
				continue;
			}
			result.add(mergedPrivilegeItem);
		}
		return result;
	}

	protected IList<ITypePrivilege> createResultByType(Collection<Class<?>> entityTypes, ISecurityScope[] securityScopes, List<Class<?>> missingEntityTypes,
			IAuthorization authorization)
	{
		ArrayList<ITypePrivilege> result = new ArrayList<ITypePrivilege>(entityTypes.size());
		String userSID = authorization.getSID();

		for (Class<?> entityType : entityTypes)
		{
			if (entityType == null)
			{
				result.add(null);
				continue;
			}
			ITypePrivilege mergedTypePrivilege = null;
			for (int a = securityScopes.length; a-- > 0;)
			{
				ITypePrivilege existingTypePrivilege = entityTypePrivilegeCache.get(entityType, securityScopes[a].getName(), userSID);
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
					throw new UnsupportedOperationException("Not yet implemented");
				}
			}
			if (mergedTypePrivilege == null)
			{
				if (missingEntityTypes != null)
				{
					missingEntityTypes.add(entityType);
				}
				else
				{
					result.add(SkipAllTypePrivilege.INSTANCE);
				}
				continue;
			}
			result.add(mergedTypePrivilege);
		}
		return result;
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId)
	{
		if (dataChange.isEmpty())
		{
			return;
		}
		writeLock.lock();
		try
		{
			privilegeCache.clear();
			entityTypePrivilegeCache.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
