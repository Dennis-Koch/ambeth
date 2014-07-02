package de.osthus.ambeth.privilege;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.datachange.IDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.DenyAllPrivilege;
import de.osthus.ambeth.privilege.model.impl.PrivilegeImpl;
import de.osthus.ambeth.privilege.model.impl.PropertyPrivilegeImpl;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.IPropertyPrivilegeOfService;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.service.IPrivilegeService;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IInterningFeature;

public class PrivilegeProvider implements IPrivilegeProvider, IInitializingBean, IDataChangeListener
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
			this.idIndex = IdIndex;
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

	public static class PrivilegeKeyOfType
	{
		public Class<?> entityType;

		public String securityScope;

		public String userSID;

		public PrivilegeKeyOfType()
		{
			// Intended blank
		}

		public PrivilegeKeyOfType(Class<?> entityType, String userSID)
		{
			this.entityType = entityType;
			this.userSID = userSID;
		}

		@Override
		public int hashCode()
		{
			if (securityScope == null)
			{
				return getClass().hashCode() ^ entityType.hashCode() ^ userSID.hashCode();
			}
			else
			{
				return getClass().hashCode() ^ entityType.hashCode() ^ userSID.hashCode() ^ securityScope.hashCode();
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (!(obj instanceof PrivilegeKeyOfType))
			{
				return false;
			}
			PrivilegeKeyOfType other = (PrivilegeKeyOfType) obj;
			return EqualsUtil.equals(entityType, other.entityType) && EqualsUtil.equals(userSID, other.userSID)
					&& EqualsUtil.equals(securityScope, other.securityScope);
		}

		@Override
		public String toString()
		{
			return "PrivilegeKeyOfType: " + entityType.getName() + " SecurityScope: '" + securityScope + "',SID:" + userSID;
		}
	}

	@LogInstance
	protected ILogger log;

	@Autowired
	protected IInterningFeature interningFeature;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired(optional = true)
	protected IPrivilegeService privilegeService;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	protected final Lock writeLock = new ReentrantLock();

	protected final HashMap<Object, IPrivilege> privilegeCache = new HashMap<Object, IPrivilege>();

	@Override
	public void afterPropertiesSet()
	{
		if (privilegeService == null && log.isDebugEnabled())
		{
			log.debug("Privilege Service could not be resolved - Privilege functionality deactivated");
		}
	}

	@Override
	public boolean isCreateAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivilege(entity, securityScopes).isCreateAllowed();
	}

	@Override
	public boolean isUpdateAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivilege(entity, securityScopes).isUpdateAllowed();
	}

	@Override
	public boolean isDeleteAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivilege(entity, securityScopes).isDeleteAllowed();
	}

	@Override
	public boolean isReadAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivilege(entity, securityScopes).isReadAllowed();
	}

	@Override
	public boolean isExecutionAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivilege(entity, securityScopes).isExecutionAllowed();
	}

	@Override
	public IPrivilege getPrivilege(Object entity, ISecurityScope... securityScopes)
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
	public IPrivilege getPrivilegeByObjRef(IObjRef objRef, ISecurityScope... securityScopes)
	{
		IList<IPrivilege> result = getPrivilegesByObjRef(new ArrayList<IObjRef>(new IObjRef[] { objRef }), securityScopes);
		if (result.size() == 0)
		{
			return DenyAllPrivilege.INSTANCE;
		}
		return result.get(0);
	}

	@Override
	public IList<IPrivilege> getPrivileges(Collection<?> entities, ISecurityScope... securityScopes)
	{
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entities, null);
		return getPrivilegesByObjRef(objRefs, securityScopes);
	}

	@Override
	public IList<IPrivilege> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, ISecurityScope... securityScopes)
	{
		IAuthorization authorization = securityScopeProvider.getAuthorization();
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

				String[] propertyPrivilegeNames = privilegeResult.getPropertyPrivilegeNames();
				IPropertyPrivilegeOfService[] propertyPrivileges = privilegeResult.getPropertyPrivileges();

				String[] propertyNames;
				IMap<String, IPropertyPrivilege> propertyPrivilegeMap;
				if (propertyPrivileges == null || propertyPrivileges.length == 0)
				{
					propertyNames = PrivilegeImpl.EMPTY_PROPERTY_NAMES;
					propertyPrivilegeMap = EmptyMap.emptyMap();
				}
				else
				{
					propertyNames = propertyPrivilegeNames;
					propertyPrivilegeMap = HashMap.create(propertyPrivileges.length);
					for (int b = propertyPrivileges.length; b-- > 0;)
					{
						IPropertyPrivilegeOfService propertyPrivilege = propertyPrivileges[b];
						String propertyName = interningFeature.intern(propertyPrivilegeNames[b]);
						propertyPrivilegeMap.put(propertyName, PropertyPrivilegeImpl.createFrom(propertyPrivilege));
					}
				}
				PrivilegeImpl pi = new PrivilegeImpl(privilegeResult.isReadAllowed(), privilegeResult.isCreateAllowed(), privilegeResult.isUpdateAllowed(),
						privilegeResult.isDeleteAllowed(), privilegeResult.isExecutionAllowed(), propertyPrivilegeMap, propertyNames);
				if (useCache)
				{
					privilegeCache.put(privilegeKey, pi);
				}
				else
				{
					if (privilegeResultOfNewEntities == null)
					{
						privilegeResultOfNewEntities = new HashMap<PrivilegeKey, IPrivilege>();
					}
					privilegeResultOfNewEntities.put(privilegeKey, pi);
				}
			}
			return createResult(objRefs, securityScopes, null, authorization, privilegeResultOfNewEntities);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IPrivilege getPrivilegeByType(Class<?> entityType, ISecurityScope... securityScopes)
	{
		IList<IPrivilege> result = getPrivilegesByType(new ArrayList<Class<?>>(new Class<?>[] { entityType }), securityScopes);
		if (result.size() == 0)
		{
			return DenyAllPrivilege.INSTANCE;
		}
		return result.get(0);
	}

	@Override
	public IList<IPrivilege> getPrivilegesByType(Collection<Class<?>> entityTypes, ISecurityScope... securityScopes)
	{
		IAuthorization authorization = securityScopeProvider.getAuthorization();
		if (authorization == null)
		{
			throw new SecurityException("User must be authenticated to be able to check for privileges");
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
			IList<IPrivilege> result = createResultByType(entityTypes, securityScopes, missingEntityTypes, authorization);
			if (missingEntityTypes.size() == 0)
			{
				return result;
			}
		}
		finally
		{
			writeLock.unlock();
		}
		String userSID = authorization.getSID();
		List<IPrivilegeOfService> privilegeResults = privilegeService.getPrivilegesOfTypes(missingEntityTypes.toArray(Class.class), securityScopes);
		writeLock.lock();
		try
		{
			for (int a = 0, size = privilegeResults.size(); a < size; a++)
			{
				IPrivilegeOfService privilegeResult = privilegeResults.get(a);
				IObjRef reference = privilegeResult.getReference();

				PrivilegeKeyOfType privilegeKey = new PrivilegeKeyOfType(reference.getRealType(), userSID);
				privilegeKey.securityScope = interningFeature.intern(privilegeResult.getSecurityScope().getName());

				String[] propertyPrivilegeNames = privilegeResult.getPropertyPrivilegeNames();
				IPropertyPrivilegeOfService[] propertyPrivileges = privilegeResult.getPropertyPrivileges();

				String[] propertyNames;
				IMap<String, IPropertyPrivilege> propertyPrivilegeMap;
				if (propertyPrivileges == null || propertyPrivileges.length == 0)
				{
					propertyNames = PrivilegeImpl.EMPTY_PROPERTY_NAMES;
					propertyPrivilegeMap = EmptyMap.emptyMap();
				}
				else
				{
					propertyNames = propertyPrivilegeNames;
					propertyPrivilegeMap = HashMap.create(propertyPrivileges.length);
					for (int b = propertyPrivileges.length; b-- > 0;)
					{
						IPropertyPrivilegeOfService propertyPrivilegeResult = propertyPrivileges[b];
						String propertyName = interningFeature.intern(propertyPrivilegeNames[b]);
						propertyPrivilegeMap.put(propertyName, PropertyPrivilegeImpl.createFrom(propertyPrivilegeResult));
					}
				}
				PrivilegeImpl pi = new PrivilegeImpl(privilegeResult.isReadAllowed(), privilegeResult.isCreateAllowed(), privilegeResult.isUpdateAllowed(),
						privilegeResult.isDeleteAllowed(), privilegeResult.isExecutionAllowed(), propertyPrivilegeMap, propertyNames);
				privilegeCache.put(privilegeKey, pi);
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

	protected IList<IPrivilege> createResultByType(Collection<Class<?>> entityTypes, ISecurityScope[] securityScopes, List<Class<?>> missingEntityTypes,
			IAuthorization authorization)
	{
		PrivilegeKeyOfType privilegeKey = null;

		ArrayList<IPrivilege> result = new ArrayList<IPrivilege>(entityTypes.size());
		String userSID = authorization.getSID();

		for (Class<?> entityType : entityTypes)
		{
			if (entityType == null)
			{
				result.add(null);
				continue;
			}
			if (privilegeKey == null)
			{
				privilegeKey = new PrivilegeKeyOfType();
			}
			privilegeKey.entityType = entityType;
			privilegeKey.userSID = userSID;

			IPrivilege mergedPrivilegeItem = null;
			for (int a = securityScopes.length; a-- > 0;)
			{
				privilegeKey.securityScope = securityScopes[a].getName();

				IPrivilege existingPrivilegeItem = privilegeCache.get(privilegeKey);
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
				if (missingEntityTypes != null)
				{
					missingEntityTypes.add(entityType);
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
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
