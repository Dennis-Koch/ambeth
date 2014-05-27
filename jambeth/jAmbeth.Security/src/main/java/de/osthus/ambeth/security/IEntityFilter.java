package de.osthus.ambeth.security;

import de.osthus.ambeth.merge.model.IObjRef;

public interface IEntityFilter
{
	ReadPermission checkReadPermissionOnEntity(Object entity, IUserHandle userHandle);

	ModifyPermission checkCreatePermissionOnEntity(IObjRef objRef, IUserHandle userHandle);

	ModifyPermission checkUpdatePermissionOnEntity(IObjRef objRef, IUserHandle userHandle);

	ModifyPermission checkDeletePermissionOnEntity(IObjRef objRef, IUserHandle userHandle);
}
