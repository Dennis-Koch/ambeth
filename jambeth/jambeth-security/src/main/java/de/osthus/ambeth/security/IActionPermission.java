package de.osthus.ambeth.security;

public interface IActionPermission
{
	public static final String ApplyType = "ApplyType";

	String getName();

	PermissionApplyType getApplyType();
}
