package com.koch.ambeth.merge.security;

import java.util.EnumSet;
import java.util.Set;

public enum SecurityDirective
{
	DISABLE_SERVICE_CHECK, //

	DISABLE_ENTITY_CHECK, //

	ENABLE_SERVICE_CHECK, //

	ENABLE_ENTITY_CHECK, //

	DISABLE_SECURITY, //

	ENABLE_SECURITY;

	private static Set<SecurityDirective> disableService = EnumSet.of(DISABLE_SERVICE_CHECK);

	private static Set<SecurityDirective> disableEntity = EnumSet.of(DISABLE_ENTITY_CHECK);

	private static Set<SecurityDirective> disableSecurity = EnumSet.of(DISABLE_SECURITY);

	private static Set<SecurityDirective> enableService = EnumSet.of(ENABLE_SERVICE_CHECK);

	private static Set<SecurityDirective> enableEntity = EnumSet.of(ENABLE_ENTITY_CHECK);

	private static Set<SecurityDirective> enableSecurity = EnumSet.of(ENABLE_SECURITY);

	private static Set<SecurityDirective> disableServiceAndEntity = EnumSet.of(DISABLE_SERVICE_CHECK, DISABLE_ENTITY_CHECK);

	public static Set<SecurityDirective> disableService()
	{
		return disableService;
	}

	public static Set<SecurityDirective> disableEntity()
	{
		return disableEntity;
	}

	public static Set<SecurityDirective> disableServiceAndEntity()
	{
		return disableServiceAndEntity;
	}

	public static Set<SecurityDirective> disableSecurity()
	{
		return disableSecurity;
	}

	public static Set<SecurityDirective> enableService()
	{
		return enableService;
	}

	public static Set<SecurityDirective> enableEntity()
	{
		return enableEntity;
	}

	public static Set<SecurityDirective> enableSecurity()
	{
		return enableSecurity;
	}
}
