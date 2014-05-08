package de.osthus.ambeth.privilege;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.datachange.IDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.model.PrivilegeEnum;
import de.osthus.ambeth.privilege.service.IPrivilegeService;
import de.osthus.ambeth.privilege.transfer.PrivilegeResult;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.ParamChecker;

public class PrivilegeProvider implements IPrivilegeProvider, IInitializingBean, IDataChangeListener
{
	public static class PrivilegeKey
	{
		public Class<?> entityType;

		public Object id;

		public byte idIndex;

		public String securityScope;

		public PrivilegeKey()
		{
			// Intended blank
		}

		public PrivilegeKey(Class<?> entityType, byte IdIndex, Object id)
		{
			this.entityType = entityType;
			this.idIndex = IdIndex;
			this.id = id;
		}

		@Override
		public int hashCode()
		{
			if (securityScope == null)
			{
				return entityType.hashCode() ^ id.hashCode();
			}
			else
			{
				return entityType.hashCode() ^ id.hashCode() ^ securityScope.hashCode();
			}
		}

		public boolean booleanEquals(Object obj)
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
					&& EqualsUtil.equals(securityScope, other.securityScope);
		}

		@Override
		public String toString()
		{
			return "CacheKey: " + entityType.getName() + "(" + idIndex + "," + id + ") SecurityScope: '" + securityScope + "'";
		}
	}

	@LogInstance
	protected ILogger log;

	protected IObjRefHelper oriHelper;

	protected IPrivilegeService privilegeService;

	protected final Lock writeLock = new ReentrantLock();

	protected final IMap<PrivilegeKey, PrivilegeEnum[]> privilegeCache = new HashMap<PrivilegeKey, PrivilegeEnum[]>();

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(oriHelper, "OriHelper");
		ParamChecker.assertNotNull(privilegeService, "PrivilegeService");
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	public void setPrivilegeService(IPrivilegeService privilegeService)
	{
		this.privilegeService = privilegeService;
	}

	@Override
	public void buildPrefetchConfig(Class<?> entityType, IPrefetchConfig prefetchConfig)
	{
		// Intended blank
	}

	@Override
	public boolean isCreateAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivileges(entity, securityScopes).isCreateAllowed();
	}

	@Override
	public boolean isUpdateAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivileges(entity, securityScopes).isUpdateAllowed();
	}

	@Override
	public boolean isDeleteAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivileges(entity, securityScopes).isDeleteAllowed();
	}

	@Override
	public boolean isReadAllowed(Object entity, ISecurityScope... securityScopes)
	{
		return getPrivileges(entity, securityScopes).isReadAllowed();
	}

	@Override
	public IPrivilegeItem getPrivileges(Object entity, ISecurityScope... securityScopes)
	{
		IList<IObjRef> objRefs = oriHelper.extractObjRefList(entity, null);
		IList<IPrivilegeItem> result = getPrivileges(objRefs, securityScopes);
		if (result.size() == 0)
		{
			return new PrivilegeItem(new PrivilegeEnum[4]);
		}
		return result.get(0);
	}

	@Override
	public IList<IPrivilegeItem> getPrivileges(List<IObjRef> objRefs, ISecurityScope... securityScopes)
	{
		ArrayList<IObjRef> missingObjRefs = new ArrayList<IObjRef>();
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			IList<IPrivilegeItem> result = createResult(objRefs, securityScopes, missingObjRefs);
			if (missingObjRefs.size() == 0)
			{
				return result;
			}
		}
		finally
		{
			writeLock.unlock();
		}
		List<PrivilegeResult> privilegeResults = privilegeService.getPrivileges(missingObjRefs.toArray(IObjRef.class), securityScopes);
		writeLock.lock();
		try
		{
			for (int a = 0, size = privilegeResults.size(); a < size; a++)
			{
				PrivilegeResult privilegeResult = privilegeResults.get(a);
				IObjRef reference = privilegeResult.getReference();

				PrivilegeKey privilegeKey = new PrivilegeKey(reference.getRealType(), reference.getIdNameIndex(), reference.getId());
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
								throw new IllegalStateException(PrivilegeEnum.class.getName() + " not supported: " + privilegeEnum);
						}
					}
				}
				privilegeCache.put(privilegeKey, indexedPrivilegeEnums);
			}
			return createResult(objRefs, securityScopes, null);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected IList<IPrivilegeItem> createResult(List<IObjRef> objRefs, ISecurityScope[] securityScopes, List<IObjRef> missingObjRefs)
	{
		PrivilegeKey privilegeKey = null;

		ArrayList<IPrivilegeItem> result = new ArrayList<IPrivilegeItem>();

		for (int b = 0, sizeB = objRefs.size(); b < sizeB; b++)
		{
			IObjRef objRef = objRefs.get(b);
			if (privilegeKey == null)
			{
				privilegeKey = new PrivilegeKey();
			}
			privilegeKey.entityType = objRef.getRealType();
			privilegeKey.idIndex = objRef.getIdNameIndex();
			privilegeKey.id = objRef.getId();

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
				continue;
			}
			privilegeKey.securityScope = null;

			result.add(new PrivilegeItem(mergedPrivilegeValues));
			privilegeKey = null;
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
