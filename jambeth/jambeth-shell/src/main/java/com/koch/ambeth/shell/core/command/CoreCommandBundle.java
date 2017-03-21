package com.koch.ambeth.shell.core.command;

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

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.koch.ambeth.shell.core.CommandBinding;
import com.koch.ambeth.shell.core.ShellContext;
import com.koch.ambeth.shell.core.annotation.Command;
import com.koch.ambeth.shell.core.annotation.CommandArg;
import com.koch.ambeth.shell.core.resulttype.CommandResult;
import com.koch.ambeth.shell.core.resulttype.ListResult;
import com.koch.ambeth.shell.core.resulttype.SingleResult;
import com.koch.ambeth.shell.util.Utils;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class CoreCommandBundle extends AbstractCommandBundle {

	@Command(name = "set", description = "set variables into the shell context")
	public CommandResult set(@CommandArg(alt = "variable=[value]", name = ".*", optional = true, //
			description = "stores variables in shell context. Use without arguments or without value to print entire context or the value of the variable to the console") //
	Entry<String, Object> entry) {
		if (entry != null) {
			if (entry.getValue() != null && entry.getKey() != null && !entry.getKey().isEmpty()) {
				shell.getContext().set(entry.getKey(), entry.getValue());
				return null;
			}
			else {
				String key = String.valueOf(entry.getValue());
				String value = "" + shell.getContext().get(key);
				if (key.matches("(?i:.*PASS.*)")) {
					value = "**********";
				}
				ListResult<String> cmdRst = new ListResult<>();
				cmdRst.addRecord(key + "=" + value);
				return cmdRst;
			}
		}
		else {

			IList<String> list = shell.getContext().getAll().keySet().toList();
			Collections.sort(list);
			ListResult<String> cmdRst = new ListResult<>();
			for (String key : list) {
				cmdRst.addRecord(key + "=" + shell.getContext().get(key));
			}
			return cmdRst;
		}

	}

	@Command(name = "echo", description = "Prints messages to the console")
	public CommandResult echo(
			@CommandArg(name = "", alt = "message", description = "the message to print") Object value) {
		return new SingleResult(shell.getContext().filter(value.toString()));
	}

	@Command(name = "wait", description = "pauses execution for the given amount of milli seconds")
	public CommandResult wait(@CommandArg String millis) {
		long lMillis = Long.valueOf(millis);
		// shell.println("Waiting until " + new Date(System.currentTimeMillis() + lMillis));
		try {
			Thread.sleep(Long.valueOf(lMillis));
		}
		catch (InterruptedException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		return null;
	}

	@Command(name = "exit", description = "exit the shell")
	public CommandResult exit(@CommandArg(optional = true, defaultValue = "0", alt = "exit-code",
			description = "the exit code") String exitStatus) {
		shell.getContext().set(ShellContext.SHUTDOWN, true);
		return null;
	}

	@Command(name = "help",
			description = "list all commands, or print details for a specific command")
	public CommandResult printHelp(@CommandArg(alt = "command-name", optional = true,
			description = "The name of a command") String commandName) {
		SingleResult cmdRst = new SingleResult();
		if (commandName != null) {
			CommandBinding commandBinding = shell.getCommandBinding(commandName);
			if (commandBinding == null) {
				throw new RuntimeException("Unknown command: " + commandName);
			}
			cmdRst.setValue(commandBinding.printHelp());
		}
		else {
			SortedMap<String, String> help = new TreeMap<>();
			int max = 0;
			for (CommandBinding cb : shell.getCommandBindings()) {
				max = cb.getName().length() > max ? cb.getName().length() : max;
				help.put(cb.getName(), cb.getDescription());
			}

			String description;
			StringBuffer strBuf = new StringBuffer();
			int i = 0;
			for (String name : help.keySet()) {
				description = help.get(name);
				strBuf.append(Utils.stringPadEnd(name, max, ' '));
				if (!description.isEmpty()) {
					strBuf.append(" - " + description);
				}
				if (i < help.keySet().size() - 1) {
					strBuf.append(System.lineSeparator());
				}
				i++;
			}
			cmdRst.setValue(strBuf.toString());
		}
		return cmdRst;
	}

	public static String readableFileSize(long size) {
		if (size <= 0) {
			return "0B";
		}
		final String[] units = new String[] {"B", "kB", "MB", "GB", "TB"};
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " "
				+ units[digitGroups];
	}
}
