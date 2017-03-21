package com.koch.ambeth.ioc.config;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;
import com.koch.ambeth.util.objectcollector.ICollectableControllerExtendable;

@ConfigurationConstants
public final class IocConfigurationConstants {
	private IocConfigurationConstants() {
	}

	/**
	 * Switches the object collector/object pool feature on ("true") or off ("false"). Default is
	 * "true".
	 *
	 * @see ICollectableControllerExtendable
	 */
	public static final String UseObjectCollector = "ambeth.ioc.objectcollector.active";

	/**
	 * Defines whether IoC classes (especially beans and properties) should save the stack trace of
	 * their declaration. If set to true the classes store a compact form of the stack trace during
	 * constructor invocation in a property, mostly called <code>declarationStackTrace</code>. Valid
	 * values: "true" or "false". Default is "false".
	 */
	public static final String TrackDeclarationTrace = "ambeth.ioc.declaration.trace.active";

	/**
	 * Allows to monitor all IoC managed beans via JMX. Valid values: "true" or "false". Default is
	 * "true".
	 */
	public static final String MonitorBeansActive = "ambeth.ioc.monitoring.active";

	/**
	 * Allows to run the IoC container in debug mode. This e.g. disables several security behaviors to
	 * ease debugging. Valid values: "true" or "false". Default is "false".
	 */
	public static final String DebugModeActive = "ambeth.ioc.debug.active";

	/**
	 * Allows Ambeth to transparently fork several algorithms to reduce execution time. This is very
	 * beneficial e.g. for large JDBC SELECT usecases or Ambeth Entity Prefetch logic. Valid values:
	 * "true" or "false". Default is "true". is "false".
	 */
	public static final String TransparentParallelizationActive =
			"ambeth.transparent.parallel.active";

	/**
	 * Allows Ambeth to work with a given classloader instance. Defaults to
	 * 'Thread.currentThread().getContextClassLoader()'
	 */
	public static final String ExplicitClassLoader = "ambeth.classloader";

	@ConfigurationConstantDescription(
			value = "Determines how the GUIThreadHelper works, if set to false, UI thread will never be used",
			defaultValue = "true")
	public static final String JavaUiActive = "ambeth.javaUi.active";
}
