package com.koch.ambeth.security;

public interface IActionPermission
{
	public static final String ApplyType = "ApplyType";

	String getName();

	PermissionApplyType getApplyType();
}
