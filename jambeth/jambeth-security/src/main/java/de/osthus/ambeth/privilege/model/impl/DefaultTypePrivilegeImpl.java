package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;

public class DefaultTypePrivilegeImpl extends AbstractTypePrivilege
{
	protected final Boolean read, create, update, delete, execute;

	protected final ITypePropertyPrivilege[] primitivePropertyPrivileges;

	protected final ITypePropertyPrivilege[] relationPropertyPrivileges;

	public DefaultTypePrivilegeImpl(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
	{
		super(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
		this.primitivePropertyPrivileges = primitivePropertyPrivileges;
		this.relationPropertyPrivileges = relationPropertyPrivileges;
	}

	@Override
	public ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return primitivePropertyPrivileges[primitiveIndex];
	}

	@Override
	public ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return relationPropertyPrivileges[relationIndex];
	}

	@Override
	public ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return null;
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
