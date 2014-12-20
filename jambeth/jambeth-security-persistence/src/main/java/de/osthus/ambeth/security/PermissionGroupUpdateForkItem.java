package de.osthus.ambeth.security;

import de.osthus.ambeth.persistence.IPermissionGroup;
import de.osthus.ambeth.persistence.ITable;

public class PermissionGroupUpdateForkItem
{
	public final String[] allSids;
	public final IPermissionGroup fPermissionGroup;
	public final ITable fTable;

	public PermissionGroupUpdateForkItem(String[] allSids, IPermissionGroup fPermissionGroup, ITable fTable)
	{
		this.allSids = allSids;
		this.fPermissionGroup = fPermissionGroup;
		this.fTable = fTable;
	}
}