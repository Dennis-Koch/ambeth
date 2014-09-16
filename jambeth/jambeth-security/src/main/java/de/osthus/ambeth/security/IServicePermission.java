package de.osthus.ambeth.security;

import java.util.regex.Pattern;

public interface IServicePermission
{
	public static final String Patterns = "Patterns";

	public static final String ApplyType = "ApplyType";

	Pattern[] getPatterns();

	PermissionApplyType getApplyType();
}
