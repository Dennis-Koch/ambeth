package de.osthus.ambeth.privilege.model;

import de.osthus.ambeth.util.IPrintable;

public class PrivilegeItem implements IPrivilegeItem, IPrintable
{
	public static final PrivilegeItem DENY_ALL = new PrivilegeItem(new PrivilegeEnum[5]);

	public static final int CREATE_INDEX = 0;

	public static final int UPDATE_INDEX = 1;

	public static final int DELETE_INDEX = 2;

	public static final int READ_INDEX = 3;

	public static final int EXECUTION_INDEX = 4;

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

	@Override
	public boolean isExecutionAllowed()
	{
		return PrivilegeEnum.EXECUTE_ALLOWED.equals(privileges[EXECUTION_INDEX]);
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
