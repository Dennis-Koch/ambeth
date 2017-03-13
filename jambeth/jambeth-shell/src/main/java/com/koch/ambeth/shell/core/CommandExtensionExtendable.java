package com.koch.ambeth.shell.core;

import java.util.List;

import com.koch.ambeth.shell.core.annotation.Command;

public interface CommandExtensionExtendable
{
	/**
	 * register commandExtension to connect to {@link Command}
	 *
	 * @param extension
	 *            instance of {@link CommandExtension}
	 * @param commandName
	 *            name of {@link Command}
	 */
	void register(CommandExtension extension, String commandName);

	/**
	 * unregister commandExtension to disconnect from {@link Command}
	 *
	 * @param extension
	 *            instance of {@link CommandExtension}
	 * @param commandName
	 *            name of {@link Command}
	 */
	void unregister(CommandExtension extension, String commandName);

	/**
	 * gets all extensions registered to a command
	 * 
	 * @param commandName
	 * @return
	 */
	List<CommandExtension> getExtensionsOfCommand(String commandName);
}
