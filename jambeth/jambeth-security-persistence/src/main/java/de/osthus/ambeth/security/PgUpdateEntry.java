package de.osthus.ambeth.security;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.persistence.IPermissionGroup;

public class PgUpdateEntry
{
	protected final IPermissionGroup permissionGroup;

	protected final Class<?> entityType;

	protected PermissionGroupUpdateType updateType = PermissionGroupUpdateType.NOTHING;

	protected IDataChange dataChange;

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

	@Override
	public String toString()
	{
		return getUpdateType().toString();
	}
}
