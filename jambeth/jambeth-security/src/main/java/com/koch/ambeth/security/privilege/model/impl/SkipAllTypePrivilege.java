package com.koch.ambeth.security.privilege.model.impl;

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;

public final class SkipAllTypePrivilege extends AbstractTypePrivilege
{
	public static final ITypePrivilege INSTANCE = new SkipAllTypePrivilege();

	private static final ITypePropertyPrivilege skipAllPropertyPrivilege = TypePropertyPrivilegeImpl.create(null, null, null, null);

	private SkipAllTypePrivilege()
	{
		super(null, null, null, null, null, null, null);
	}

	@Override
	public ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public Boolean isCreateAllowed()
	{
		return null;
	}

	@Override
	public Boolean isReadAllowed()
	{
		return null;
	}

	@Override
	public Boolean isUpdateAllowed()
	{
		return null;
	}

	@Override
	public Boolean isDeleteAllowed()
	{
		return null;
	}

	@Override
	public Boolean isExecuteAllowed()
	{
		return null;
	}
}
