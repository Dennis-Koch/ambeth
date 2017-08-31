package com.koch.ambeth.log;

/*-
 * #%L
 * jambeth-log
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

import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public class LogConfigurationConstants {
	/**
	 * The path to the file to which Ambeth should write the log statements. No default value, if not
	 * set Ambeth will not log to file.
	 */
	public static final String LogFile = "ambeth.log.file";

	public static final String LoggerType = "ambeth.log.class";

	private LogConfigurationConstants() {
		// Intended blank
	}
}
