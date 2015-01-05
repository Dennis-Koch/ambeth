package de.osthus.ambeth.security;


public class PermissionGroupUpdateForkItem
{
	public final String[] allSids;
	public final PgUpdateEntry pgUpdateEntry;

	public PermissionGroupUpdateForkItem(String[] allSids, PgUpdateEntry pgUpdateEntry)
	{
		this.allSids = allSids;
		this.pgUpdateEntry = pgUpdateEntry;
	}
}