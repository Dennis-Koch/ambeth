package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPropertyPrivilege;

public class DefaultPrivilegeImpl extends AbstractPrivilege
{
	protected boolean read, create, update, delete, execute;

	protected final IPropertyPrivilege[] primitivePropertyPrivileges;

	protected final IPropertyPrivilege[] relationPropertyPrivileges;

	public DefaultPrivilegeImpl(boolean create, boolean read, boolean update, boolean delete, boolean execute,
			IPropertyPrivilege[] primitivePropertyPrivileges, IPropertyPrivilege[] relationPropertyPrivileges)
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
	public IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return primitivePropertyPrivileges[primitiveIndex];
	}

	@Override
	public void setPrimitivePropertyPrivilege(int primitiveIndex, IPropertyPrivilege propertyPrivilege)
	{
		primitivePropertyPrivileges[primitiveIndex] = propertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return relationPropertyPrivileges[relationIndex];
	}

	@Override
	public void setRelationPropertyPrivilege(int relationIndex, IPropertyPrivilege propertyPrivilege)
	{
		relationPropertyPrivileges[relationIndex] = propertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return null;
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
