package de.osthus.ambeth.security;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.exceptions.ServiceCallForbiddenException;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeSecurityManager;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.privilege.IPrivilegeItem;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtension;
import de.osthus.ambeth.privilege.IPrivilegeProviderExtensionExtendable;
import de.osthus.ambeth.privilege.model.ReadPermission;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class SecurityManager implements ISecurityManager, IMergeSecurityManager, IPrivilegeProviderExtensionExtendable, IServiceFilterExtendable,
		IEntityFilterExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	// protected final DefaultExtendableContainer<IEntityFilter> entityFilters = new DefaultExtendableContainer<IEntityFilter>(IEntityFilter.class,
	// "entityFilter");

	protected final DefaultExtendableContainer<IServiceFilter> serviceFilters = new DefaultExtendableContainer<IServiceFilter>(IServiceFilter.class,
			"serviceFilter");

	protected final Lock readLock, writeLock;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPrivilegeProvider privilegeProvider;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	public SecurityManager()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T filterValue(T value)
	{
		IdentityHashMap<Object, ReadPermission> alreadyProcessedMap = new IdentityHashMap<Object, ReadPermission>();

		return (T) filterValue(value, alreadyProcessedMap, securityScopeProvider.getUserHandle(), securityScopeProvider.getSecurityScopes(), null);
	}

	protected Class<?> getTypeOfValue(Object value)
	{
		if (value instanceof IObjRef)
		{
			return ((IObjRef) value).getRealType();
		}
		return value.getClass();
	}

	@SuppressWarnings("unchecked")
	protected Object filterList(List<?> list, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle, ISecurityScope[] securityScopes,
			IEntityFilter[] entityFilters)
	{
		Collection<Object> cloneCollection;
		try
		{
			cloneCollection = list.getClass().newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (list.size() == 0)
		{
			return cloneCollection;
		}
		Object firstItem = list.get(0);
		IList<IPrivilegeItem> privileges;
		if (firstItem instanceof IObjRef)
		{
			privileges = privilegeProvider.getPrivilegesByObjRef((Collection<IObjRef>) list, securityScopes);
		}
		else
		{
			privileges = privilegeProvider.getPrivileges(list, securityScopes);
		}
		for (int a = 0, size = list.size(); a < size; a++)
		{
			Object item = list.get(a);
			if (item == null)
			{
				cloneCollection.add(null);
				continue;
			}
			IPrivilegeItem privilege = privileges.get(a);
			if (privilege.isReadAllowed())
			{
				cloneCollection.add(item);
			}
			// Object filteredItem = filterValue(item, alreadyProcessedMap, userHandle, entityFilters);
			// if (filteredItem == item)
			// {
			// // Filtering ok and unchanged
			// cloneCollection.add(filteredItem);
			// continue;
			// }
			// // Item got replaced
			// if (item != null)
			// {
			// cloneCollection.add(filteredItem);
			// }
		}
		return cloneCollection;
	}

	protected Object filterValue(Object value, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle, ISecurityScope[] securityScopes,
			IEntityFilter[] entityFilters)
	{
		if (value == null)
		{
			return null;
		}
		ReadPermission existingReadPermission = alreadyProcessedMap.get(value);
		if (existingReadPermission != null)
		{
			// Object has already been processes regarding visibility check
			if (ReadPermission.FORBIDDEN.equals(existingReadPermission))
			{
				return null;
			}
			return value;
		}
		if (value instanceof List)
		{
			return filterList((List<?>) value, alreadyProcessedMap, userHandle, securityScopes, entityFilters);
		}
		else if (value instanceof Collection)
		{
			return filterCollection((Collection<?>) value, alreadyProcessedMap, userHandle, securityScopes, entityFilters);
		}
		else if (value.getClass().isArray())
		{
			int length = Array.getLength(value);
			ArrayList<Object> tempList = new ArrayList<Object>(length);
			for (int a = 0, size = length; a < size; a++)
			{
				Object item = Array.get(value, a);
				Object filteredItem = filterValue(item, alreadyProcessedMap, userHandle, securityScopes, entityFilters);
				if (filteredItem == item)
				{
					// Filtering ok and unchanged
					tempList.add(filteredItem);
					continue;
				}
				// Item got replaced
				if (item != null)
				{
					tempList.add(filteredItem);
				}
			}
			Object cloneArray = Array.newInstance(value.getClass().getComponentType(), tempList.size());
			for (int a = tempList.size(); a-- > 0;)
			{
				Array.set(cloneArray, a, tempList.get(a));
			}
			return cloneArray;
		}
		Class<?> type = getTypeOfValue(value);
		if (entityMetaDataProvider.getMetaData(type, true) != null)
		{
			ReadPermission readPermission = filterEntity(value, alreadyProcessedMap, userHandle, entityFilters);
			switch (readPermission)
			{
				case PARTLY_ALLOWED:

					// Fall through intended
				case ALLOWED:
					return value;
				default:
					return null;
			}
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	protected Object filterCollection(Collection<?> coll, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle,
			ISecurityScope[] securityScopes, IEntityFilter[] entityFilters)
	{
		Collection<Object> cloneCollection;
		try
		{
			cloneCollection = coll.getClass().newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		Iterator<?> iter = coll.iterator();
		if (!iter.hasNext())
		{
			return cloneCollection;
		}
		Object firstItem = iter.next();
		IList<IPrivilegeItem> privileges;
		if (firstItem instanceof IObjRef)
		{
			privileges = privilegeProvider.getPrivilegesByObjRef((Collection<IObjRef>) coll, securityScopes);
		}
		else
		{
			privileges = privilegeProvider.getPrivileges(coll, securityScopes);
		}
		int index = -1;
		while (iter.hasNext())
		{
			Object item = iter.next();
			index++;
			if (item == null)
			{
				cloneCollection.add(null);
				continue;
			}
			IPrivilegeItem privilege = privileges.get(index);
			if (privilege.isReadAllowed())
			{
				cloneCollection.add(item);
			}
			// Object filteredItem = filterValue(item, alreadyProcessedMap, userHandle, entityFilters);
			// if (filteredItem == item)
			// {
			// // Filtering ok and unchanged
			// cloneCollection.add(filteredItem);
			// continue;
			// }
			// // Item got replaced
			// if (item != null)
			// {
			// cloneCollection.add(filteredItem);
			// }
		}
		if (iter instanceof IDisposable)
		{
			((IDisposable) iter).dispose();
		}
		return cloneCollection;

	}

	protected ReadPermission filterEntity(Object entity, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle, IEntityFilter[] entityFilters)
	{
		ReadPermission cachedPermission = alreadyProcessedMap.get(entity);
		if (cachedPermission != null)
		{
			return cachedPermission;
		}
		ReadPermission restrictiveReadPermission = ReadPermission.ALLOWED;
		for (IEntityFilter entityFilter : entityFilters)
		{
			ReadPermission readPermission = entityFilter.checkReadPermissionOnEntity(entity, userHandle);
			switch (readPermission)
			{
				case UNDEFINED:
					break;
				case FORBIDDEN:
					// Forbid and return if one filter forbids
					return ReadPermission.FORBIDDEN;
				case PARTLY_ALLOWED:
					// Restrict partly if one filter retricts
					restrictiveReadPermission = readPermission;
					break;
				case ALLOWED:
					break;
				default:
					throw RuntimeExceptionUtil.createEnumNotSupportedException(readPermission);
			}
		}
		alreadyProcessedMap.put(entity, restrictiveReadPermission);
		return restrictiveReadPermission;
	}

	// protected void FilterEntityChildren(Object entity, IDictionary<Object, ReadPermission> alreadyProcessedSet, IUserHandle userHandle)
	// {
	// PropertyInfo[] properties = entity.GetType().GetProperties(BindingFlags.FlattenHierarchy | BindingFlags.Instance | BindingFlags.Public);
	// foreach (PropertyInfo property in properties)
	// {
	// Object value = property.GetValue(entity, null);
	// Object filteredValue = FilterValue(value, alreadyProcessedSet, userHandle);
	// if (filteredValue == value)
	// {
	// continue; // Nothing to do
	// }
	// property.SetValue(entity, filteredValue, null);
	// }
	// }

	@Override
	public void checkServiceAccess(Method method, Object[] arguments, SecurityContextType securityContextType, IUserHandle userHandle)
	{
		CallPermission callPermission = filterService(method, arguments, securityContextType, userHandle);
		if (callPermission == CallPermission.FORBIDDEN)
		{
			throw new ServiceCallForbiddenException(StringBuilderUtil.concat(objectCollector, "For current user with sid '",
					userHandle != null ? userHandle.getSID() : "n/a", "' it is not permitted to call service ", method.getDeclaringClass().getName(), ".",
					method.getName()));
		}
	}

	@Override
	public void checkMergeAccess(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		IMap<Class<?>, List<IChangeContainer>> typeToChanges = buildTypeToChanges(cudResult.getAllChanges());

		for (Entry<Class<?>, List<IChangeContainer>> entry : typeToChanges)
		{
			Class<?> entityType = entry.getKey();

		}
	}

	protected IMap<Class<?>, List<IChangeContainer>> buildTypeToChanges(List<IChangeContainer> allChanges)
	{
		HashMap<Class<?>, List<IChangeContainer>> typeToChanges = new HashMap<Class<?>, List<IChangeContainer>>();

		for (int a = allChanges.size(); a-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			IObjRef objRef = changeContainer.getReference();
			List<IChangeContainer> changes = typeToChanges.get(objRef.getRealType());
			if (changes == null)
			{
				changes = new ArrayList<IChangeContainer>();
				typeToChanges.put(objRef.getRealType(), changes);
			}
			changes.add(changeContainer);
		}
		return typeToChanges;
	}

	protected CallPermission filterService(Method method, Object[] arguments, SecurityContextType securityContextType, IUserHandle userHandle)
	{
		CallPermission restrictiveCallPermission = CallPermission.ALLOWED;
		for (IServiceFilter serviceFilter : serviceFilters.getExtensions())
		{
			CallPermission callPermission = serviceFilter.checkCallPermissionOnService(method, arguments, securityContextType, userHandle);
			switch (callPermission)
			{
				case UNDEFINED:
					break;
				case FORBIDDEN:
					return callPermission;
				case ALLOWED:
					break;
				default:
					throw new IllegalStateException("Enum " + callPermission + " not supported");
			}
		}
		return restrictiveCallPermission;
	}

	@Override
	public void registerPrivilegeProviderExtension(IPrivilegeProviderExtension privilegeProviderExtension, Class<?> entityType)
	{
	}

	@Override
	public void unregisterPrivilegeProviderExtension(IPrivilegeProviderExtension privilegeProviderExtension, Class<?> entityType)
	{
	}

	@Override
	public void registerEntityFilter(IEntityFilter entityFilter)
	{
		// entityFilters.register(entityFilter);
	}

	@Override
	public void unregisterEntityFilter(IEntityFilter entityFilter)
	{
		// entityFilters.unregister(entityFilter);
	}

	@Override
	public void registerServiceFilter(IServiceFilter serviceFilter)
	{
		serviceFilters.register(serviceFilter);
	}

	@Override
	public void unregisterServiceFilter(IServiceFilter serviceFilter)
	{
		serviceFilters.unregister(serviceFilter);
	}
}
