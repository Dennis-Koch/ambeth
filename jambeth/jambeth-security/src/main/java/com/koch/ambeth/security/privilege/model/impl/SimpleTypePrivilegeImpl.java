package com.koch.ambeth.security.privilege.model.impl;

import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;

public class SimpleTypePrivilegeImpl extends AbstractTypePrivilege
{
	private final Boolean create, read, update, delete, execute;

	private final ITypePropertyPrivilege defaultPropertyPrivileges;

	public SimpleTypePrivilegeImpl(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege defaultPropertyPrivileges)
	{
		super(create, read, update, delete, execute, null, null);
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
		this.defaultPropertyPrivileges = defaultPropertyPrivileges;
	}

	@Override
	public ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return defaultPropertyPrivileges;
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
