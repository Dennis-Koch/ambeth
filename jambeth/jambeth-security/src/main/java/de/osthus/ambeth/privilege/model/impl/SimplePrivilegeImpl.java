package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.util.IPrintable;

public class SimplePrivilegeImpl implements IPrivilege, IPrintable
{
	protected boolean create, read, update, delete, execute;

	protected final IPropertyPrivilege defaultPropertyPrivileges;

	public SimplePrivilegeImpl(boolean create, boolean read, boolean update, boolean delete, boolean execute, IPropertyPrivilege defaultPropertyPrivileges)
	{
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

	public void setCreateAllowed(boolean create)
	{
		this.create = create;
	}

	@Override
	public boolean isReadAllowed()
	{
		return read;
	}

	public void setReadAllowed(boolean read)
	{
		this.read = read;
	}

	@Override
	public boolean isUpdateAllowed()
	{
		return update;
	}

	public void setUpdateAllowed(boolean update)
	{
		this.update = update;
	}

	@Override
	public boolean isDeleteAllowed()
	{
		return delete;
	}

	public void setDeleteAllowed(boolean delete)
	{
		this.delete = delete;
	}

	@Override
	public boolean isExecuteAllowed()
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
		sb.append(isReadAllowed() ? "+R" : "-R");
		sb.append(isCreateAllowed() ? "+C" : "-C");
		sb.append(isUpdateAllowed() ? "+U" : "-U");
		sb.append(isDeleteAllowed() ? "+D" : "-D");
		sb.append(isExecuteAllowed() ? "+X" : "-X");
	}
}
