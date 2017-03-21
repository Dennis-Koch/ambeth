package com.koch.ambeth.shell.core;

/*-
 * #%L
 * jambeth-shell
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

import java.util.List;

/**
 *
 * Binds methods to commands and generates help and usage info
 *
 * @author daniel.mueller
 *
 */
public interface CommandBinding {
	/**
	 *
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	Object execute(List<String> arguments);

	/**
	 *
	 * @return
	 */
	String getName();

	/**
	 *
	 * @return
	 */
	String getDescription();

	/**
	 *
	 */
	String printHelp();

	/**
	 *
	 */
	String printUsage();
}
