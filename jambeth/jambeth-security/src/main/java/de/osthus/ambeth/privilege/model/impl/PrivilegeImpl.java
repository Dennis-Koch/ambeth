package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class PrivilegeImpl implements IPrivilege, IPrintable
{
	public static final String[] EMPTY_PROPERTY_NAMES = new String[0];

	protected boolean read, create, update, delete, execute;

	protected final IMap<String, IPropertyPrivilege> propertyPrivilegeMap;

	protected final String[] propertyNames;

	public PrivilegeImpl(boolean read, boolean create, boolean update, boolean delete, boolean execute)
	{
		this(read, create, update, delete, execute, EmptyMap.<String, IPropertyPrivilege> emptyMap(), EMPTY_PROPERTY_NAMES);
	}

	public PrivilegeImpl(boolean read, boolean create, boolean update, boolean delete, boolean execute, IMap<String, IPropertyPrivilege> propertyPrivilegeMap,
			String[] propertyNames)
	{
		this.read = read;
		this.create = create;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
		this.propertyPrivilegeMap = propertyPrivilegeMap;
		this.propertyNames = propertyNames;
	}

	@Override
	public IPropertyPrivilege getPropertyPrivilege(String propertyName)
	{
		return propertyPrivilegeMap.get(propertyName);
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
	public boolean isExecutionAllowed()
	{
		return execute;
	}

	public void setExecutionAllowed(boolean execute)
	{
		this.execute = execute;
	}

	@Override
	public String[] getConfiguredPropertyNames()
	{
		return propertyNames;
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
		sb.append(isExecutionAllowed() ? "+X" : "-X");

		for (String configuredPropertyName : getConfiguredPropertyNames())
		{
			IPropertyPrivilege propertyPrivilege = propertyPrivilegeMap.get(configuredPropertyName);
			StringBuilderUtil.appendPrintable(sb, propertyPrivilege);
		}
	}
}
