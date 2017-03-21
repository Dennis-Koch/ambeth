package com.koch.ambeth.merge.security;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.EnumSet;
import java.util.Set;

public enum SecurityDirective {
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

	private static Set<SecurityDirective> disableServiceAndEntity =
			EnumSet.of(DISABLE_SERVICE_CHECK, DISABLE_ENTITY_CHECK);

	public static Set<SecurityDirective> disableService() {
		return disableService;
	}

	public static Set<SecurityDirective> disableEntity() {
		return disableEntity;
	}

	public static Set<SecurityDirective> disableServiceAndEntity() {
		return disableServiceAndEntity;
	}

	public static Set<SecurityDirective> disableSecurity() {
		return disableSecurity;
	}

	public static Set<SecurityDirective> enableService() {
		return enableService;
	}

	public static Set<SecurityDirective> enableEntity() {
		return enableEntity;
	}

	public static Set<SecurityDirective> enableSecurity() {
		return enableSecurity;
	}
}
