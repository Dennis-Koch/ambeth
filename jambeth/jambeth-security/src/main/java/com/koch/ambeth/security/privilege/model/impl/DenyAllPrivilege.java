package com.koch.ambeth.security.privilege.model.impl;

import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;

public final class DenyAllPrivilege extends AbstractPrivilege
{
	public static final IPrivilege INSTANCE = new DenyAllPrivilege();

	private static final IPropertyPrivilege denyAllPropertyPrivilege = PropertyPrivilegeImpl.create(false, false, false, false);

	private DenyAllPrivilege()
	{
		super(false, false, false, false, false, null, null);
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
}
