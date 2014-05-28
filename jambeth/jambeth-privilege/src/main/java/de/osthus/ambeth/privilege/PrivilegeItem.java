package de.osthus.ambeth.privilege;

import de.osthus.ambeth.privilege.model.PrivilegeEnum;

public class PrivilegeItem implements IPrivilegeItem
{
	public static final int CREATE_INDEX = 0;

	public static final int UPDATE_INDEX = 1;

	public static final int DELETE_INDEX = 2;

	public static final int READ_INDEX = 3;

	protected final PrivilegeEnum[] privileges;

	public PrivilegeItem(PrivilegeEnum[] privileges)
	{
		this.privileges = privileges;
	}

	@Override
	public boolean isCreateAllowed()
	{
		return PrivilegeEnum.CREATE_ALLOWED.equals(privileges[CREATE_INDEX]);
	}

	@Override
	public boolean isUpdateAllowed()
	{
		return PrivilegeEnum.UPDATE_ALLOWED.equals(privileges[UPDATE_INDEX]);
	}

	@Override
	public boolean isDeleteAllowed()
	{
		return PrivilegeEnum.DELETE_ALLOWED.equals(privileges[DELETE_INDEX]);
	}

	@Override
	public boolean isReadAllowed()
	{
		return PrivilegeEnum.READ_ALLOWED.equals(privileges[READ_INDEX]);
	}
}
