package de.osthus.ambeth.security;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.IPermissionGroup;

public class PgUpdateEntry
{
	protected final IPermissionGroup permissionGroup;

	protected final Class<?> entityType;

	protected PermissionGroupUpdateType updateType = PermissionGroupUpdateType.NOTHING;

	protected IDataChange dataChange;

	protected IList<IObjRef> objRefs;

	protected IList<Object> permissionGroupIds;

	protected int startIndexInAllObjRefs;

	public PgUpdateEntry(Class<?> entityType, IPermissionGroup permissionGroup)
	{
		this.entityType = entityType;
		this.permissionGroup = permissionGroup;
	}

	public IPermissionGroup getPermissionGroup()
	{
		return permissionGroup;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public PermissionGroupUpdateType getUpdateType()
	{
		return updateType;
	}

	public void setUpdateType(PermissionGroupUpdateType updateType)
	{
		this.updateType = updateType;
	}

	public IDataChange getDataChange()
	{
		return dataChange;
	}

	public void setDataChange(IDataChange dataChange)
	{
		this.dataChange = dataChange;
	}

	public IList<IObjRef> getObjRefs()
	{
		return objRefs;
	}

	public void setObjRefs(IList<IObjRef> objRefs)
	{
		this.objRefs = objRefs;
	}

	public IList<Object> getPermissionGroupIds()
	{
		return permissionGroupIds;
	}

	public void setPermissionGroupIds(IList<Object> permissionGroupIds)
	{
		this.permissionGroupIds = permissionGroupIds;
	}

	@Override
	public String toString()
	{
		return getUpdateType().toString();
	}

	public int getStartIndexInAllObjRefs()
	{
		return startIndexInAllObjRefs;
	}

	public void setStartIndexInAllObjRefs(int startIndexInAllObjRefs)
	{
		this.startIndexInAllObjRefs = startIndexInAllObjRefs;
	}
}
