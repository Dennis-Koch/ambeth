package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.util.IPrintable;

public final class AllowAllPrivilege implements IPrivilege, IPrintable
{
	public static final IPrivilege INSTANCE = new AllowAllPrivilege();

	private static final IPropertyPrivilege allowAllPropertyPrivilege = PropertyPrivilegeImpl.create(true, true, true, true);

	private AllowAllPrivilege()
	{
		// intended blank
	}

	@Override
	public IPropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return allowAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return allowAllPropertyPrivilege;
	}

	@Override
	public IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return allowAllPropertyPrivilege;
	}

	@Override
	public boolean isCreateAllowed()
	{
		return true;
	}

	@Override
	public boolean isReadAllowed()
	{
		return true;
	}

	@Override
	public boolean isUpdateAllowed()
	{
		return true;
	}

	@Override
	public boolean isDeleteAllowed()
	{
		return true;
	}

	@Override
	public boolean isExecuteAllowed()
	{
		return true;
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
