package de.osthus.ambeth.privilege;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.datachange.IDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.IPrivilegeItem;
import de.osthus.ambeth.privilege.model.PrivilegeEnum;
import de.osthus.ambeth.privilege.model.PrivilegeItem;
import de.osthus.ambeth.privilege.transfer.PrivilegeResult;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.IUserHandle;
import de.osthus.ambeth.service.IPrivilegeService;
import de.osthus.ambeth.util.EqualsUtil;

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
				return entityType.hashCode() ^ id.hashCode() ^ userSID.hashCode();
			}
			else
			{
				return entityType.hashCode() ^ id.hashCode() ^ userSID.hashCode() ^ securityScope.hashCode();
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
	protected IObjRefHelper objRefHelper;

	@Autowired(optional = true)
	protected IPrivilegeService privilegeService;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	protected final Lock writeLock = new ReentrantLock();

	protected final HashMap<PrivilegeKey, PrivilegeEnum[]> privilegeCache = new HashMap<PrivilegeKey, PrivilegeEnum[]>();

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
	public IPrivilegeItem getPrivilege(Object entity, ISecurityScope... securityScopes)
	{
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entity, null);
		IList<IPrivilegeItem> result = getPrivileges(objRefs, securityScopes);
		if (result.size() == 0)
		{
			return new PrivilegeItem(new PrivilegeEnum[4]);
		}
		return result.get(0);
	}

	@Override
	public IPrivilegeItem getPrivilegeByObjRef(IObjRef objRef, ISecurityScope... securityScopes)
	{
		IList<IPrivilegeItem> result = getPrivilegesByObjRef(new ArrayList<IObjRef>(new IObjRef[] { objRef }), securityScopes);
		if (result.size() == 0)
		{
			return new PrivilegeItem(new PrivilegeEnum[4]);
		}
		return result.get(0);
	}

	@Override
	public IList<IPrivilegeItem> getPrivileges(Collection<?> entities, ISecurityScope... securityScopes)
	{
		IList<IObjRef> objRefs = objRefHelper.extractObjRefList(entities, null);
		return getPrivilegesByObjRef(objRefs, securityScopes);
	}

	@Override
	public IList<IPrivilegeItem> getPrivilegesByObjRef(Collection<? extends IObjRef> objRefs, ISecurityScope... securityScopes)
	{
		IUserHandle userHandle = securityScopeProvider.getUserHandle();
		if (userHandle == null)
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
			IList<IPrivilegeItem> result = createResult(objRefs, securityScopes, missingObjRefs, userHandle);
			if (missingObjRefs.size() == 0)
			{
				return result;
			}
		}
		finally
		{
			writeLock.unlock();
		}
		String userSID = userHandle.getSID();
		List<PrivilegeResult> privilegeResults = privilegeService.getPrivileges(missingObjRefs.toArray(IObjRef.class), securityScopes);
		writeLock.lock();
		try
		{
			for (int a = 0, size = privilegeResults.size(); a < size; a++)
			{
				PrivilegeResult privilegeResult = privilegeResults.get(a);
				IObjRef reference = privilegeResult.getReference();

				PrivilegeKey privilegeKey = new PrivilegeKey(reference.getRealType(), reference.getIdNameIndex(), reference.getId(), userSID);
				privilegeKey.securityScope = privilegeResult.getSecurityScope().getName();

				PrivilegeEnum[] privilegeEnums = privilegeResult.getPrivileges();

				PrivilegeEnum[] indexedPrivilegeEnums = new PrivilegeEnum[4];
				if (privilegeEnums != null)
				{
					for (int b = privilegeEnums.length; b-- > 0;)
					{
						PrivilegeEnum privilegeEnum = privilegeEnums[b];
						switch (privilegeEnum)
						{
							case NONE:
							{
								break;
							}
							case CREATE_ALLOWED:
							{
								indexedPrivilegeEnums[PrivilegeItem.CREATE_INDEX] = privilegeEnum;
								break;
							}
							case UPDATE_ALLOWED:
							{
								indexedPrivilegeEnums[PrivilegeItem.UPDATE_INDEX] = privilegeEnum;
								break;
							}
							case DELETE_ALLOWED:
							{
								indexedPrivilegeEnums[PrivilegeItem.DELETE_INDEX] = privilegeEnum;
								break;
							}
							case READ_ALLOWED:
							{
								indexedPrivilegeEnums[PrivilegeItem.READ_INDEX] = privilegeEnum;
								break;
							}
							default:
								throw RuntimeExceptionUtil.createEnumNotSupportedException(privilegeEnum);
						}
					}
				}
				privilegeCache.put(privilegeKey, indexedPrivilegeEnums);
			}
			return createResult(objRefs, securityScopes, null, userHandle);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected IList<IPrivilegeItem> createResult(Collection<? extends IObjRef> objRefs, ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs,
			IUserHandle userHandle)
	{
		PrivilegeKey privilegeKey = null;

		ArrayList<IPrivilegeItem> result = new ArrayList<IPrivilegeItem>(objRefs.size());
		String userSID = userHandle.getSID();

		for (IObjRef objRef : objRefs)
		{
			if (privilegeKey == null)
			{
				privilegeKey = new PrivilegeKey();
			}
			privilegeKey.entityType = objRef.getRealType();
			privilegeKey.idIndex = objRef.getIdNameIndex();
			privilegeKey.id = objRef.getId();
			privilegeKey.userSID = userSID;

			PrivilegeEnum[] mergedPrivilegeValues = null;
			for (int a = securityScopes.length; a-- > 0;)
			{
				privilegeKey.securityScope = securityScopes[a].getName();

				PrivilegeEnum[] existingPrivilegeValues = privilegeCache.get(privilegeKey);
				if (existingPrivilegeValues == null)
				{
					mergedPrivilegeValues = null;
					break;
				}
				if (mergedPrivilegeValues == null)
				{
					// Take first existing privilege as a start
					mergedPrivilegeValues = new PrivilegeEnum[existingPrivilegeValues.length];
					System.arraycopy(existingPrivilegeValues, 0, mergedPrivilegeValues, 0, existingPrivilegeValues.length);
				}
				else
				{
					// Merge all other existing privileges by boolean OR
					for (int c = mergedPrivilegeValues.length; c-- > 0;)
					{
						PrivilegeEnum existingPrivilegeValue = existingPrivilegeValues[c];
						if (!PrivilegeEnum.NONE.equals(existingPrivilegeValue))
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
					missingObjRefs.add(objRef);
				}
				else
				{
					result.add(PrivilegeItem.DENY_ALL);
				}
				continue;
			}
			result.add(new PrivilegeItem(mergedPrivilegeValues));
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
