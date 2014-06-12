package de.osthus.ambeth.security;

import java.util.regex.Pattern;

public interface IServicePermission
{
	Pattern[] getPatterns();

	PermissionApplyType getApplyType();
}
