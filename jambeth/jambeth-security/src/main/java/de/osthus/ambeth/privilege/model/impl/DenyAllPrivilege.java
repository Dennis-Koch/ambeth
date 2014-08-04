package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.util.IPrintable;

public final class DenyAllPrivilege implements IPrivilege, IPrintable
{
	public static final IPrivilege INSTANCE = new DenyAllPrivilege();

	private static final IPropertyPrivilege denyAllPropertyPrivilege = PropertyPrivilegeImpl.create(false, false, false, false);

	private DenyAllPrivilege()
	{
		// intended blank
	}

	@Override
	public IPropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return denyAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return denyAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return denyAllPropertyPrivilege;
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
	public boolean isExecuteAllowed()
	{
		return false;
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
