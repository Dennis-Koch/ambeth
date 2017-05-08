package com.koch.ambeth.shell;

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

import java.io.BufferedReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Collection;

import com.koch.ambeth.shell.core.CommandBinding;
import com.koch.ambeth.shell.core.ShellContext;
import com.koch.ambeth.shell.core.resulttype.CommandResult;

/**
 *
 * @author daniel.mueller
 *
 */
public interface AmbethShell {
	public static final String PROPERTY_SHELL_MODE = "SHELL_MODE";
	public static final String MODE_INTERACTIVE = "INTERACTIVE";
	public static final String MODE_SERVICE = "SERVICE";
	public static final String SHELL_CONTEXT_BASE_FOLDER = "shell.context.base.folder";

	/**
	 *
	 * @return
	 */
	ShellContext getContext();

	/**
	 *
	 */
	void startInteractive(BufferedReader br);

	/**
	 *
	 * @param args
	 * @return {@link CommandResult}
	 */
	CommandResult executeCommand(String... args);

	/**
	 * executes an unparsed (i.e. not processed command)
	 *
	 * @param unparsedCommandLine
	 * @return {@link CommandResult}
	 */
	CommandResult executeRawCommand(String unparsedCommandLine);

	/**
	 *
	 * @param name
	 * @return
	 */
	CommandBinding getCommandBinding(String commandName);

	/**
	 *
	 */
	Collection<CommandBinding> getCommandBindings();

	/**
	 *
	 * @param object
	 */
	void print(Object object);

	/**
	 *
	 * @param object
	 */
	void println(Object object);

	/**
	 *
	 */
	void println();

	/**
	 * Deprecated: use context.set(ShellContext.SHUTDOWN, true) instead
	 *
	 * @param object
	 */
	@Deprecated
	void exit(int status);

	/**
	 * all commands should use the same Date format
	 *
	 * @return
	 */
	DateFormat getDateFormat();

	/**
	 * set prompt value to prompt information map
	 *
	 * @param key key
	 * @param value value
	 */
	void setPrompt(String key, String value);

	/**
	 * remove prompt value from prompt information map
	 *
	 * @param key key
	 */
	void removePrompt(String key);

	/**
	 *
	 * @return
	 */
	String getPromptString();

	// /**
	// * register a PrintStream to which Commands print their results This is handled by a ThreadLocal
	// *
	// * @param ps
	// */
	// void registerSystemOut(PrintStream ps);
	/**
	 * get the current shell output stream
	 *
	 * @param key key
	 */
	PrintStream getShellOut();

	/**
	 * set the current shell output stream
	 */
	void setShellOut(PrintStream shellOut);

	/**
	 * expecting a fileName, a dir can't be handled for now
	 *
	 * @param fileName
	 * @return
	 */
	Path getSecuredFileAsPath(String fileName);

	String getSecuredFileAsString(String fileName);

	Path getSecuredFileAsPath(Path resolve);
}
