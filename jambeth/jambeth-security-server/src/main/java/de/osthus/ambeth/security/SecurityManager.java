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
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
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
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.privilege.IPrivilegeProviderIntern;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.ReadPermission;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class SecurityManager implements ISecurityManager, IMergeSecurityManager, IServiceFilterExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final DefaultExtendableContainer<IServiceFilter> serviceFilters = new DefaultExtendableContainer<IServiceFilter>(IServiceFilter.class,
			"serviceFilter");

	protected final Lock readLock, writeLock;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPrivilegeProviderIntern privilegeProvider;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

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

		return (T) filterValue(value, alreadyProcessedMap, securityContextHolder.getCreateContext().getAuthorization(),
				securityScopeProvider.getSecurityScopes());
	}

	@SuppressWarnings("unchecked")
	protected Object filterList(List<?> list, Map<Object, ReadPermission> alreadyProcessedMap, IAuthorization authorization, ISecurityScope[] securityScopes)
	{
		if (list.size() == 0)
		{
			// nothing to filter
			return list;
		}
		Object firstItem = list.get(0);
		IList<IPrivilege> privileges;
		if (firstItem instanceof IObjRef)
		{
			privileges = privilegeProvider.getPrivilegesByObjRef((Collection<IObjRef>) list, securityScopes);
		}
		else if (firstItem != null && entityMetaDataProvider.getMetaData(firstItem.getClass(), true) != null)
		{
			privileges = privilegeProvider.getPrivileges(list, securityScopes);
		}
		else
		{
			// nothing to filter
			return list;
		}
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
				cloneCollection.add(null);
				continue;
			}
			IPrivilege privilege = privileges.get(a);
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

	protected Object filterValue(Object value, Map<Object, ReadPermission> alreadyProcessedMap, IAuthorization authorization, ISecurityScope[] securityScopes)
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
			return filterList((List<?>) value, alreadyProcessedMap, authorization, securityScopes);
		}
		else if (value instanceof Collection)
		{
			return filterCollection((Collection<?>) value, alreadyProcessedMap, authorization, securityScopes);
		}
		else if (value.getClass().isArray())
		{
			int length = Array.getLength(value);
			ArrayList<Object> tempList = new ArrayList<Object>(length);
			for (int a = 0, size = length; a < size; a++)
			{
				Object item = Array.get(value, a);
				Object filteredItem = filterValue(item, alreadyProcessedMap, authorization, securityScopes);
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
		if (!(value instanceof IObjRef) && entityMetaDataProvider.getMetaData(value.getClass(), true) == null)
		{
			// not an entity. So nothing to filter
			return value;
		}
		ReadPermission readPermission = filterEntity(value, alreadyProcessedMap, authorization, securityScopes);
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

	@SuppressWarnings("unchecked")
	protected Object filterCollection(Collection<?> coll, Map<Object, ReadPermission> alreadyProcessedMap, IAuthorization authorization,
			ISecurityScope[] securityScopes)
	{
		if (coll.size() == 0)
		{
			// nothing to filter
			return coll;
		}
		Iterator<?> iter = coll.iterator();
		if (!iter.hasNext())
		{
			throw new IllegalStateException("Must never happen");
		}
		Object firstItem = iter.next();
		IList<IPrivilege> privileges;
		if (firstItem instanceof IObjRef)
		{
			privileges = privilegeProvider.getPrivilegesByObjRef((Collection<IObjRef>) coll, securityScopes);
		}
		else if (firstItem != null && entityMetaDataProvider.getMetaData(firstItem.getClass(), true) != null)
		{
			privileges = privilegeProvider.getPrivileges(coll, securityScopes);
		}
		else
		{
			// nothing to filter
			return coll;
		}
		Collection<Object> cloneCollection;
		try
		{
			cloneCollection = coll.getClass().newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
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
			IPrivilege privilege = privileges.get(index);
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

	protected ReadPermission filterEntity(Object entity, Map<Object, ReadPermission> alreadyProcessedMap, IAuthorization authorization,
			ISecurityScope[] securityScopes)
	{
		IPrivilege privilege = privilegeProvider.getPrivilege(entity, securityScopes);
		ReadPermission rp;
		if (privilege == null || privilege.isReadAllowed())
		{
			rp = ReadPermission.ALLOWED;
		}
		else
		{
			rp = ReadPermission.FORBIDDEN;
		}
		alreadyProcessedMap.put(entity, rp);
		return rp;
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
	public void checkMethodAccess(Method method, Object[] arguments, SecurityContextType securityContextType, IAuthorization authorization)
	{
		CallPermission callPermission = filterService(method, arguments, securityContextType, authorization);
		if (callPermission == CallPermission.FORBIDDEN)
		{
			throw new ServiceCallForbiddenException(StringBuilderUtil.concat(objectCollector, "For current user with sid '",
					authorization != null ? authorization.getSID() : "n/a", "' it is not permitted to call service ", method.getDeclaringClass().getName(),
					".", method.getName()));
		}
	}

	@Override
	public void checkMergeAccess(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		if (!securityActivation.isSecured())
		{
			return;
		}
		ISet<IObjRef> relatedObjRefs = scanForAllObjRefs(cudResult);

		IList<IObjRef> relatedObjRefsList = relatedObjRefs.toList();
		IList<IPrivilege> privilegeItems = privilegeProvider.getPrivilegesByObjRef(relatedObjRefsList, securityScopeProvider.getSecurityScopes());
		HashMap<IObjRef, IPrivilege> objRefToPrivilege = HashMap.<IObjRef, IPrivilege> create(relatedObjRefsList.size());

		for (int a = relatedObjRefsList.size(); a-- > 0;)
		{
			IObjRef objRef = relatedObjRefsList.get(a);
			IPrivilege privilegeItem = privilegeItems.get(a);
			objRefToPrivilege.put(objRef, privilegeItem);
		}
		evaluatePermssionOnAllObjRefs(cudResult, objRefToPrivilege);
	}

	protected ISet<IObjRef> scanForAllObjRefs(ICUDResult cudResult)
	{
		HashSet<IObjRef> relatedObjRefs = new HashSet<IObjRef>();

		List<Object> originalRefs = cudResult.getOriginalRefs();
		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		for (int a = allChanges.size(); a-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			IObjRef reference = changeContainer.getReference();

			if (reference instanceof IDirectObjRef && ((IDirectObjRef) reference).getDirect() instanceof IChangeContainer)
			{
				Object directEntity = originalRefs.get(((IDirectObjRef) reference).getCreateContainerIndex());
				relatedObjRefs.add(new DirectObjRef(reference.getRealType(), directEntity));
			}
			else
			{
				relatedObjRefs.add(reference);
			}
			IRelationUpdateItem[] ruis = null;
			if (changeContainer instanceof CreateContainer)
			{
				ruis = ((CreateContainer) changeContainer).getRelations();
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				ruis = ((UpdateContainer) changeContainer).getRelations();
			}
			if (ruis == null)
			{
				continue;
			}
			for (IRelationUpdateItem rui : ruis)
			{
				addRelatedObjRefs(rui.getAddedORIs(), relatedObjRefs, originalRefs);
				addRelatedObjRefs(rui.getRemovedORIs(), relatedObjRefs, originalRefs);
			}
		}
		return relatedObjRefs;
	}

	protected void evaluatePermssionOnAllObjRefs(ICUDResult cudResult, Map<IObjRef, IPrivilege> objRefToPrivilege)
	{
		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		for (int a = allChanges.size(); a-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			IObjRef reference = changeContainer.getReference();

			IPrivilege privilege = objRefToPrivilege.get(reference);

			if (!privilege.isReadAllowed())
			{
				// just for robustness
				throw new SecurityException("Current user has no permission to read entity and is therefore not allowed to imply any change: " + reference);
			}

			IRelationUpdateItem[] ruis = null;
			if (changeContainer instanceof CreateContainer)
			{
				if (!privilege.isCreateAllowed())
				{
					throw new SecurityException("Current user has no permission to create entity: " + reference);
				}
				evaluatePermissionOnEntityCreate((CreateContainer) changeContainer, privilege);
				ruis = ((CreateContainer) changeContainer).getRelations();
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				if (!privilege.isUpdateAllowed())
				{
					throw new SecurityException("Current user has no permission to update entity: " + reference);
				}
				evaluatePermissionOnEntityUpdate((UpdateContainer) changeContainer, privilege);
				ruis = ((UpdateContainer) changeContainer).getRelations();
			}
			else if (!privilege.isDeleteAllowed())
			{
				throw new SecurityException("Current user has no permission to delete entity: " + reference);
			}
			if (ruis == null)
			{
				continue;
			}
			for (IRelationUpdateItem rui : ruis)
			{
				evaulatePermissionOnRelatedObjRefs(rui.getAddedORIs(), objRefToPrivilege);
				evaulatePermissionOnRelatedObjRefs(rui.getRemovedORIs(), objRefToPrivilege);
			}
		}
	}

	protected void evaluatePermissionOnEntityCreate(CreateContainer changeContainer, IPrivilege privilege)
	{
		IPropertyPrivilege defaultPropertyPrivilege = privilege.getDefaultPropertyPrivilegeIfValid();
		IEntityMetaData metaData = defaultPropertyPrivilege == null ? entityMetaDataProvider.getMetaData(changeContainer.getReference().getRealType()) : null;
		IPrimitiveUpdateItem[] primitives = changeContainer.getPrimitives();
		IRelationUpdateItem[] relations = changeContainer.getRelations();
		if (primitives != null)
		{
			for (IPrimitiveUpdateItem pui : primitives)
			{
				IPropertyPrivilege propertyPrivilege;
				if (metaData != null)
				{
					int primitiveIndex = metaData.getIndexByPrimitiveName(pui.getMemberName());
					propertyPrivilege = privilege.getPrimitivePropertyPrivilege(primitiveIndex);
				}
				else
				{
					propertyPrivilege = defaultPropertyPrivilege;
				}
				boolean createPrivilege = propertyPrivilege != null ? propertyPrivilege.isCreateAllowed() : true;
				if (!createPrivilege)
				{
					throw new SecurityException("Current user has no permssion to create property '" + pui.getMemberName() + "' on entity: "
							+ changeContainer.getReference());
				}
			}
		}
		if (relations != null)
		{
			for (IRelationUpdateItem rui : relations)
			{
				IPropertyPrivilege propertyPrivilege;
				if (metaData != null)
				{
					int relationIndex = metaData.getIndexByRelationName(rui.getMemberName());
					propertyPrivilege = privilege.getRelationPropertyPrivilege(relationIndex);
				}
				else
				{
					propertyPrivilege = defaultPropertyPrivilege;
				}
				boolean createPrivilege = propertyPrivilege != null ? propertyPrivilege.isCreateAllowed() : true;
				if (!createPrivilege)
				{
					throw new SecurityException("Current user has no permssion to create property '" + rui.getMemberName() + "' on entity: "
							+ changeContainer.getReference());
				}
			}
		}
	}

	protected void evaluatePermissionOnEntityUpdate(UpdateContainer changeContainer, IPrivilege privilege)
	{
		IPropertyPrivilege defaultPropertyPrivilege = privilege.getDefaultPropertyPrivilegeIfValid();
		IEntityMetaData metaData = defaultPropertyPrivilege == null ? entityMetaDataProvider.getMetaData(changeContainer.getReference().getRealType()) : null;
		IPrimitiveUpdateItem[] primitives = changeContainer.getPrimitives();
		IRelationUpdateItem[] relations = changeContainer.getRelations();
		if (primitives != null)
		{
			for (IPrimitiveUpdateItem pui : primitives)
			{
				IPropertyPrivilege propertyPrivilege;
				if (metaData != null)
				{
					int primitiveIndex = metaData.getIndexByPrimitiveName(pui.getMemberName());
					propertyPrivilege = privilege.getPrimitivePropertyPrivilege(primitiveIndex);
				}
				else
				{
					propertyPrivilege = defaultPropertyPrivilege;
				}
				boolean updatePrivilege = propertyPrivilege != null ? propertyPrivilege.isUpdateAllowed() : true;
				if (!updatePrivilege)
				{
					throw new SecurityException("Current user has no permssion to update property '" + pui.getMemberName() + "' on entity: "
							+ changeContainer.getReference());
				}
			}
		}
		if (relations != null)
		{
			for (IRelationUpdateItem rui : relations)
			{
				IPropertyPrivilege propertyPrivilege;
				if (metaData != null)
				{
					int relationIndex = metaData.getIndexByRelationName(rui.getMemberName());
					propertyPrivilege = privilege.getRelationPropertyPrivilege(relationIndex);
				}
				else
				{
					propertyPrivilege = defaultPropertyPrivilege;
				}
				boolean updatePrivilege = propertyPrivilege != null ? propertyPrivilege.isUpdateAllowed() : true;
				if (!updatePrivilege)
				{
					throw new SecurityException("Current user has no permssion to update property '" + rui.getMemberName() + "' on entity: "
							+ changeContainer.getReference());
				}
			}
		}
	}

	protected void addRelatedObjRefs(IObjRef[] objRefs, ISet<IObjRef> relatedObjRefs, List<Object> originalRefs)
	{
		if (objRefs == null)
		{
			return;
		}
		for (IObjRef objRef : objRefs)
		{
			if (objRef instanceof IDirectObjRef && ((IDirectObjRef) objRef).getDirect() instanceof IChangeContainer)
			{
				Object directEntity = originalRefs.get(((IDirectObjRef) objRef).getCreateContainerIndex());
				relatedObjRefs.add(new DirectObjRef(objRef.getRealType(), directEntity));
			}
			else
			{
				relatedObjRefs.add(objRef);
			}
		}
	}

	protected void evaulatePermissionOnRelatedObjRefs(IObjRef[] objRefs, Map<IObjRef, IPrivilege> objRefToPrivilege)
	{
		if (objRefs == null)
		{
			return;
		}
		for (IObjRef objRef : objRefs)
		{
			IPrivilege privilege = objRefToPrivilege.get(objRef);

			if (!privilege.isReadAllowed())
			{
				// just for robustness
				throw new SecurityException(
						"Current user has no permssion to read entity and is therefore not allowed to imply any change where this entity is involved: "
								+ objRef);
			}
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

	protected CallPermission filterService(Method method, Object[] arguments, SecurityContextType securityContextType, IAuthorization authorization)
	{
		ISecurityScope[] securityScopes = securityScopeProvider.getSecurityScopes();
		CallPermission restrictiveCallPermission = CallPermission.ALLOWED;
		for (IServiceFilter serviceFilter : serviceFilters.getExtensions())
		{
			CallPermission callPermission = serviceFilter.checkCallPermissionOnService(method, arguments, securityContextType, authorization, securityScopes);
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
