package de.osthus.ambeth.privilege;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.Tuple3KeyHashMap;
import de.osthus.ambeth.collections.Tuple5KeyHashMap;
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
import de.osthus.ambeth.privilege.model.IPrivilegeResult;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilegeResult;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.DenyAllPrivilege;
import de.osthus.ambeth.privilege.model.impl.PrivilegeResult;
import de.osthus.ambeth.privilege.model.impl.PropertyPrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.SimplePrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.SimpleTypePrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.SkipAllTypePrivilege;
import de.osthus.ambeth.privilege.model.impl.TypePrivilegeResult;
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
import de.osthus.ambeth.util.IInterningFeature;

public class PrivilegeProvider implements IPrivilegeProviderIntern, IInitializingBean, IDataChangeListener
{
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

	protected final Tuple5KeyHashMap<Class<?>, Object, Byte, String, String, IPrivilege> privilegeCache = new Tuple5KeyHashMap<Class<?>, Object, Byte, String, String, IPrivilege>();

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
		throw new UnsupportedOperationException("Not implemented");
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
		IPrivilegeResult result = getPrivileges(objRefs, securityScopes);
		return result.getPrivileges()[0];
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
		IPrivilegeResult result = getPrivilegesByObjRef(new ArrayList<IObjRef>(new IObjRef[] { objRef }), securityScopes);
		return result.getPrivileges()[0];
	}

	@Override
	public IPrivilegeResult getPrivileges(List<?> entities)
	{
		return getPrivileges(entities, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilegeResult getPrivileges(List<?> entities, ISecurityScope[] securityScopes)
	{
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entities, null);
		return getPrivilegesByObjRef(objRefs, securityScopes);
	}

	@Override
	public IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs)
	{
		return getPrivilegesByObjRef(objRefs, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs, IPrivilegeCache privilegeCache)
	{
		return getPrivilegesByObjRef(objRefs);
	}

	@Override
	public IPrivilegeResult getPrivilegesByObjRef(List<? extends IObjRef> objRefs, ISecurityScope[] securityScopes)
	{
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (authorization == null)
		{
			throw new SecurityException("User must be authenticated to be able to check for privileges");
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
			IPrivilegeResult result = createResult(objRefs, securityScopes, missingObjRefs, authorization, null);
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
			Tuple5KeyHashMap<Class<?>, Object, Byte, String, String, IPrivilege> privilegeResultOfNewEntities = null;
			for (int a = 0, size = privilegeResults.size(); a < size; a++)
			{
				IPrivilegeOfService privilegeResult = privilegeResults.get(a);
				IObjRef reference = privilegeResult.getReference();

				boolean useCache = true;
				Object id = reference.getId();
				Byte idIndex = Byte.valueOf(reference.getIdNameIndex());
				if (id == null)
				{
					useCache = false;
					id = reference;
				}
				String securityScope = interningFeature.intern(privilegeResult.getSecurityScope().getName());

				IPrivilege privilege = createPrivilegeFromServiceResult(reference, privilegeResult);
				if (useCache)
				{
					privilegeCache.put(reference.getRealType(), id, idIndex, userSID, securityScope, privilege);
				}
				else
				{
					if (privilegeResultOfNewEntities == null)
					{
						privilegeResultOfNewEntities = new Tuple5KeyHashMap<Class<?>, Object, Byte, String, String, IPrivilege>();
					}
					privilegeResultOfNewEntities.put(reference.getRealType(), id, idIndex, userSID, securityScope, privilege);
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

		if (propertyPrivilegesOfService == null || propertyPrivilegesOfService.length == 0)
		{
			return SimplePrivilegeImpl.createFrom(privilegeOfService);
		}
		String[] propertyPrivilegeNames = privilegeOfService.getPropertyPrivilegeNames();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
		IPropertyPrivilege[] primitivePropertyPrivileges = new IPropertyPrivilege[metaData.getPrimitiveMembers().length];
		IPropertyPrivilege[] relationPropertyPrivileges = new IPropertyPrivilege[metaData.getRelationMembers().length];
		IPropertyPrivilege defaultPropertyPrivilege = PropertyPrivilegeImpl.createFrom(privilegeOfService);
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
		ITypePrivilegeResult result = getPrivilegesByType(new ArrayList<Class<?>>(new Class<?>[] { entityType }), securityScopes);
		return result.getTypePrivileges()[0];
	}

	@Override
	public ITypePrivilegeResult getPrivilegesByType(List<Class<?>> entityTypes)
	{
		return getPrivilegesByType(entityTypes, securityScopeProvider.getSecurityScopes());
	}

	@Override
	public ITypePrivilegeResult getPrivilegesByType(List<Class<?>> entityTypes, ISecurityScope[] securityScopes)
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
			ITypePrivilegeResult result = createResultByType(entityTypes, securityScopes, missingEntityTypes, authorization);
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

	protected IPrivilegeResult createResult(List<? extends IObjRef> objRefs, ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs,
			IAuthorization authorization, Tuple5KeyHashMap<Class<?>, Object, Byte, String, String, IPrivilege> privilegeResultOfNewEntities)
	{
		IPrivilege[] result = new IPrivilege[objRefs.size()];
		String userSID = authorization.getSID();

		String[] securityScopesNames = new String[securityScopes.length];
		for (int a = securityScopes.length; a-- > 0;)
		{
			securityScopesNames[a] = interningFeature.intern(securityScopes[a].getName());
		}
		for (int index = objRefs.size(); index-- > 0;)
		{
			IObjRef objRef = objRefs.get(index);
			if (objRef == null)
			{
				continue;
			}
			boolean useCache = true;
			Class<?> entityType = objRef.getRealType();
			Byte idIndex = Byte.valueOf(objRef.getIdNameIndex());
			Object id = objRef.getId();

			if (id == null)
			{
				useCache = false;
				// use the ObjRef instance as the id
				id = objRef;
			}

			IPrivilege mergedPrivilegeItem = null;
			for (int a = securityScopesNames.length; a-- > 0;)
			{
				String securityScope = securityScopesNames[a];

				IPrivilege existingPrivilegeItem = useCache ? privilegeCache.get(entityType, id, idIndex, userSID, securityScope)
						: privilegeResultOfNewEntities != null ? privilegeResultOfNewEntities.get(entityType, id, idIndex, userSID, securityScope) : null;
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
					continue;
				}
				mergedPrivilegeItem = DenyAllPrivilege.INSTANCE;
			}
			result[index] = mergedPrivilegeItem;
		}
		return new PrivilegeResult(authorization.getSID(), result);
	}

	protected ITypePrivilegeResult createResultByType(List<Class<?>> entityTypes, ISecurityScope[] securityScopes, List<Class<?>> missingEntityTypes,
			IAuthorization authorization)
	{
		ITypePrivilege[] result = new ITypePrivilege[entityTypes.size()];
		String userSID = authorization.getSID();

		for (int index = entityTypes.size(); index-- > 0;)
		{
			Class<?> entityType = entityTypes.get(index);
			if (entityType == null)
			{
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
					continue;
				}
				mergedTypePrivilege = SkipAllTypePrivilege.INSTANCE;
			}
			result[index] = mergedTypePrivilege;
		}
		return new TypePrivilegeResult(authorization.getSID(), result);
	}

	public void handleClearAllCaches(ClearAllCachesEvent evnt)
	{
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

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId)
	{
		if (dataChange.isEmpty())
		{
			return;
		}
		handleClearAllCaches(null);
	}
}
