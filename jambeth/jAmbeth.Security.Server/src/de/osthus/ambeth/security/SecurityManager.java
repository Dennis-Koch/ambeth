package de.osthus.ambeth.security;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.exceptions.ServiceCallForbiddenException;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class SecurityManager implements ISecurityManager, IServiceFilterExtendable, IEntityFilterExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final DefaultExtendableContainer<IEntityFilter> entityFilters = new DefaultExtendableContainer<IEntityFilter>(IEntityFilter.class, "entityFilter");

	protected final DefaultExtendableContainer<IServiceFilter> serviceFilters = new DefaultExtendableContainer<IServiceFilter>(IServiceFilter.class,
			"serviceFilter");

	protected final Lock readLock, writeLock;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

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
		return (T) filterValue(value, alreadyProcessedMap, securityScopeProvider.getUserHandle());
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
	protected Object filterList(List<?> list, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle)
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
		for (int a = 0, size = list.size(); a < size; a++)
		{
			Object item = list.get(a);
			if (item == null)
			{
				continue;
			}
			Object filteredItem = filterValue(item, alreadyProcessedMap, userHandle);
			if (filteredItem == item)
			{
				// Filtering ok and unchanged
				cloneCollection.add(filteredItem);
				continue;
			}
			// Item got replaced
			if (item != null)
			{
				cloneCollection.add(filteredItem);
			}
		}
		return cloneCollection;
	}

	@SuppressWarnings("unchecked")
	protected Object filterValue(Object value, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle)
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
			return filterList((List<?>) value, alreadyProcessedMap, userHandle);
		}
		else if (value instanceof Collection)
		{
			return filterCollection((Collection<?>) value, alreadyProcessedMap, userHandle);
		}
		else if (value.getClass().isArray())
		{
			int length = Array.getLength(value);
			ArrayList<Object> tempList = new ArrayList<Object>(length);
			for (int a = 0, size = length; a < size; a++)
			{
				Object item = Array.get(value, a);
				Object filteredItem = filterValue(item, alreadyProcessedMap, userHandle);
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
			ReadPermission readPermission = filterEntity(value, alreadyProcessedMap, userHandle);
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
	protected Object filterCollection(Collection<?> value, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle)
	{
		Iterator<?> iter = value.iterator();

		Collection<Object> cloneCollection;
		try
		{
			cloneCollection = value.getClass().newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

		while (iter.hasNext())
		{
			Object item = iter.next();
			if (item == null)
			{
				continue;
			}
			Object filteredItem = filterValue(item, alreadyProcessedMap, userHandle);
			if (filteredItem == item)
			{
				// Filtering ok and unchanged
				cloneCollection.add(filteredItem);
				continue;
			}
			// Item got replaced
			if (item != null)
			{
				cloneCollection.add(filteredItem);
			}
		}
		if (iter instanceof IDisposable)
		{
			((IDisposable) iter).dispose();
		}
		return cloneCollection;

	}

	protected ReadPermission filterEntity(Object entity, Map<Object, ReadPermission> alreadyProcessedMap, IUserHandle userHandle)
	{
		ReadPermission cachedPermission = alreadyProcessedMap.get(entity);
		if (cachedPermission != null)
		{
			return cachedPermission;
		}
		ReadPermission restrictiveReadPermission = ReadPermission.ALLOWED;
		for (IEntityFilter entityFilter : entityFilters.getExtensions())
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
			throw new ServiceCallForbiddenException(StringBuilderUtil.concat(objectCollector, "For current user with sid '", userHandle.getSID(),
					"' it is not permitted to call service ", method.getDeclaringClass().getName(), ".", method.getName()));
		}
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
	public void registerEntityFilter(IEntityFilter entityFilter)
	{
		entityFilters.register(entityFilter);
	}

	@Override
	public void unregisterEntityFilter(IEntityFilter entityFilter)
	{
		entityFilters.unregister(entityFilter);
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
