package com.koch.ambeth.security.privilege.model.impl;

import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;

public class DefaultTypePrivilegeSimpleImpl extends AbstractTypePrivilege
{
	protected final Boolean read, create, update, delete, execute;

	protected final ITypePropertyPrivilege propertyPrivilege;

	public DefaultTypePrivilegeSimpleImpl(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege propertyPrivilege)
	{
		super(create, read, update, delete, execute, null, null);
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
		this.propertyPrivilege = propertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return propertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return propertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return propertyPrivilege;
	}

	@Override
	public Boolean isCreateAllowed()
	{
		return create;
	}

	@Override
	public Boolean isReadAllowed()
	{
		return read;
	}

	@Override
	public Boolean isUpdateAllowed()
	{
		return update;
	}

	@Override
	public Boolean isDeleteAllowed()
	{
		return delete;
	}

	@Override
	public Boolean isExecuteAllowed()
	{
		return execute;
	}
}
