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

import com.koch.ambeth.util.collections.IMap;

public interface ShellContext {
	static final String BATCH_FILE = "batch.file";
	// static final String EXIT_ON_ERROR = "exit.on.error";
	static final String GREETING_ACTIVE = "greeting.active";
	static final String LICENSE_EXPIRATION_DATE = "license.expiration.date";
	static final String LICENSE_TEXT = "license.text";
	static final String LICENSE_TYPE = "license.type";
	static final String MAIN_ARGS = "main.args";
	static final String PRODUCT_NAME = "product.name";
	static final String PRODUCT_VERSION = "product.version";
	static final String PROMPT = "shell.prompt";
	static final String SHUTDOWN = "shutdown.requested";
	static final String VAR_MARKER = "$";
	static final String VARS_FOR_BATCH_FILE = "batch.file.variables";
	static final String VERBOSE = "verbose";
	static final String HIDE_IO = "hide.io";
	static final String ERROR_MODE = "error.mode";

	enum ErrorMode {
		EXIT_ON_ERROR, THROW_EXCPETION, LOG_ONLY
	}

	String filter(String input);

	Object get(String key);

	<T> T get(String key, Class<T> expectedType);

	<T> T get(String key, T defaultValue);

	IMap<String, Object> getAll();

	void remove(String key);

	Object resolve(String key);

	void set(String key, Object value);
}
