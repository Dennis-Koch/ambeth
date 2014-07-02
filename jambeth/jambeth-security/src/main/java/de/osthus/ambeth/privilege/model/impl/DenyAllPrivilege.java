package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.util.IPrintable;

public final class DenyAllPrivilege implements IPrivilege, IPrintable
{
	public static final IPrivilege INSTANCE = new DenyAllPrivilege();

	private DenyAllPrivilege()
	{
		// intended blank
	}

	@Override
	public IPropertyPrivilege getPropertyPrivilege(String propertyName)
	{
		return null;
	}

	@Override
	public boolean isCreateAllowed()
	{
		return false;
	}

	@Override
	public boolean isReadAllowed()
	{
		return false;
	}

	@Override
	public boolean isUpdateAllowed()
	{
		return false;
	}

	@Override
	public boolean isDeleteAllowed()
	{
		return false;
	}

	@Override
	public boolean isExecutionAllowed()
	{
		return false;
	}

	@Override
	public String[] getConfiguredPropertyNames()
	{
		return PrivilegeImpl.EMPTY_PROPERTY_NAMES;
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
	}
}
