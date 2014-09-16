package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;

public final class AllowAllPrivilege extends AbstractPrivilege
{
	public static final IPrivilege INSTANCE = new AllowAllPrivilege();

	private static final IPropertyPrivilege allowAllPropertyPrivilege = PropertyPrivilegeImpl.create(true, true, true, true);

	private AllowAllPrivilege()
	{
		super(true, true, true, true, true, null, null);
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
}
