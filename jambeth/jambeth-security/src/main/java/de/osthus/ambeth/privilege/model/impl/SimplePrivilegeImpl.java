package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPropertyPrivilege;

public class SimplePrivilegeImpl extends AbstractPrivilege
{
	protected final boolean create, read, update, delete, execute;

	protected final IPropertyPrivilege defaultPropertyPrivileges;

	public SimplePrivilegeImpl(boolean create, boolean read, boolean update, boolean delete, boolean execute, IPropertyPrivilege defaultPropertyPrivileges)
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
	public IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public IPropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public boolean isCreateAllowed()
	{
		return create;
	}

	@Override
	public boolean isReadAllowed()
	{
		return read;
	}

	@Override
	public boolean isUpdateAllowed()
	{
		return update;
	}

	@Override
	public boolean isDeleteAllowed()
	{
		return delete;
	}

	@Override
	public boolean isExecuteAllowed()
	{
		return execute;
	}
}
