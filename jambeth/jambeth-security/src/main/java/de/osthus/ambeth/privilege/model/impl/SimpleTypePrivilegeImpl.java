package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.util.IPrintable;

public class SimpleTypePrivilegeImpl implements ITypePrivilege, IPrintable
{
	protected Boolean create, read, update, delete, execute;

	protected final ITypePropertyPrivilege defaultPropertyPrivileges;

	public SimpleTypePrivilegeImpl(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege defaultPropertyPrivileges)
	{
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

	public void setCreateAllowed(Boolean create)
	{
		this.create = create;
	}

	@Override
	public Boolean isReadAllowed()
	{
		return read;
	}

	public void setReadAllowed(Boolean read)
	{
		this.read = read;
	}

	@Override
	public Boolean isUpdateAllowed()
	{
		return update;
	}

	public void setUpdateAllowed(Boolean update)
	{
		this.update = update;
	}

	@Override
	public Boolean isDeleteAllowed()
	{
		return delete;
	}

	public void setDeleteAllowed(boolean delete)
	{
		this.delete = delete;
	}

	@Override
	public Boolean isExecuteAllowed()
	{
		return execute;
	}

	public void setExecuteAllowed(boolean execute)
	{
		this.execute = execute;
	}

	@Override
	public final String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(AbstractPrivilege.upperOrLower(isCreateAllowed(), 'c'));
		sb.append(AbstractPrivilege.upperOrLower(isReadAllowed(), 'r'));
		sb.append(AbstractPrivilege.upperOrLower(isUpdateAllowed(), 'u'));
		sb.append(AbstractPrivilege.upperOrLower(isDeleteAllowed(), 'd'));
		sb.append(AbstractPrivilege.upperOrLower(isExecuteAllowed(), 'e'));
	}
}
