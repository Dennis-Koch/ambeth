package de.osthus.ambeth.security;

public class PermissionGroupUpdateForkItem
{
	public final PgUpdateEntry pgUpdateEntry;
	public final IAuthentication[] authentications;
	public final IAuthorization[] authorizations;

	public PermissionGroupUpdateForkItem(IAuthentication[] authentications, IAuthorization[] authorizations, PgUpdateEntry pgUpdateEntry)
	{
		this.authentications = authentications;
		this.authorizations = authorizations;
		this.pgUpdateEntry = pgUpdateEntry;
	}
}