package de.osthus.ambeth.shell.core;

import de.osthus.ambeth.collections.IMap;

public interface ShellContext
{
	static final String VAR_MARKER = "$";
	static final String VERBOSE = "verbose";
	static final String BATCH_FILE = "batch.file";
	static final String MAIN_ARGS = "main.args";
	static final String EXIT_ON_ERROR = "exit.on.error";
	static final String PROMPT = "shell.prompt";
	static final String VARS_FOR_BATCH_FILE = "batch.file.variables";

	void set(String key, Object value);

	void remove(String key);

	Object get(String key);

	<T> T get(String key, Class<T> expectedType);

	<T> T get(String key, T defaultValue);

	IMap<String, Object> getAll();

	Object resolve(String key);

	String filter(String input);
}
