package com.koch.ambeth.core.config;

/*-
 * #%L
 * jambeth-core
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

import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.core.start.IClasspathInfo;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class CoreConfigurationConstants {
	/**
	 * Defines which implementation of the {@link IClasspathScanner} is used during startup. The
	 * default is the {@link com.koch.ambeth.core.start.CoreClasspathScanner}.
	 */
	public static final String ClasspathScannerClass = "ambeth.classpath.scanner.class";

	/**
	 * Defines which implementation of the {@link IClasspathInfo} is used during startup. The default
	 * is the {@link com.koch.ambeth.core.start.SystemClasspathInfo}.
	 */
	public static final String ClasspathInfoClass = "ambeth.classpath.info.class";

	/**
	 * Defines which path to scan for the plugin jars, every path separated by semicolon, for example:
	 * c:\dev\plugins;c:\dev\others\hello.jar
	 */
	public static final String PluginPaths = "ambeth.classpath.plugin.paths";

	/**
	 * if this property set true, then the can path sub folder will be scaned
	 */
	public static final String PluginPathsRecursiveFlag =
			"ambeth.classpath.plugin.paths.recursive.flag";

	/**
	 * Defines in which packages the Ambeth {@link IClasspathScanner} scans for classes which
	 * implement or are annotated with a given class (e.g {@link XmlRootElement},
	 * {@link ApplicationModule}). Multiple patterns can be provided by separating them with a
	 * semicolon ';', each pattern has to be a regular expression. The default pattern is
	 * <code>"com/koch.*"</code>
	 */
	public static final String PackageScanPatterns = "ambeth.classpath.scanner.pattern";

	private CoreConfigurationConstants() {
		// Intended blank
	}
}
