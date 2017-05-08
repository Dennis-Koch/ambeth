package com.koch.ambeth.server.rest.config;

/*-
 * #%L
 * jambeth-server-rest
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

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class WebServiceConfigurationConstants {
	private WebServiceConfigurationConstants() {
		// intended blank
	}

	@ConfigurationConstantDescription("TODO")
	public static final String SessionAuthorizationTimeToLive = "ambeth.session.authorization.ttl";

	/**
	 * Configure whether or not Ambeth does a classpath scan at web application startup to find its
	 * modules.
	 * <p>
	 * Valid values: "true" or "false". Default is "true".
	 */
	public static final String ClasspathScanning = "ambeth.rest.classpath-scanning";

	/**
	 * A fully qualified class name of {@link IBundleModule} to be used for framwork context creation.
	 * <p>
	 * May be set set if {@link #ClasspathScanning} is set to false. If not set,
	 * {@link Ambeth#createEmpty()} is used to create the initial framework context.
	 * <p>
	 * Must not be set if {@link #ClasspathScanning} is set to true.
	 * <p>
	 * Please note: If {@link #FrameworkBundle} is set a class path scan will be performed for
	 * application modules though even {@link #ApplicationModules} is set or not. To configure what
	 * packages are scanned add the {@link CoreConfigurationConstants#PackageScanPatterns} parameter.
	 * <p>
	 * To avoid class path scanning at all do not set this parameter.
	 */
	public static final String FrameworkBundle = "ambeth.rest.framework-bundle";

	/**
	 * A semicolon separated list of fully qualified class names of {@link IInitializingModule}s to be
	 * included for framework context creation.
	 * <p>
	 * If set, given modules will additionally added to framework context whether a classpath scan is
	 * performed or not..
	 */
	public static final String FrameworkModules = "ambeth.rest.framework-modules";

	/**
	 * A semicolon separated list of fully qualified class names of {@link IInitializingModule}s to be
	 * included for application context creation.
	 * <p>
	 * If set, given modules will additionally added to application context whether a classpath scan
	 * is performed or not.
	 */
	public static final String ApplicationModules = "ambeth.rest.application-modules";
}
