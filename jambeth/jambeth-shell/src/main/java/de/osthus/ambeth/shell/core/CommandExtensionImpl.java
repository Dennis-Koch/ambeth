package de.osthus.ambeth.shell.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for CommandExtensionExtendable
 */
public class CommandExtensionImpl implements CommandExtensionExtendable
{

	protected final Map<String, List<CommandExtension>> commandExtensions = new HashMap<String, List<CommandExtension>>();

	@Override
	public void register(CommandExtension extension, String commandName)
	{
		List<CommandExtension> list = commandExtensions.get(commandName);
		if (list == null)
		{
			list = new ArrayList<CommandExtension>();
		}
		list.add(extension);
		if (!commandExtensions.containsKey(commandName))
		{
			commandExtensions.put(commandName, list);
		}
	}

	@Override
	public void unregister(CommandExtension extension, String commandName)
	{
		commandExtensions.get(commandName).remove(extension);
	}

	@Override
	public List<CommandExtension> getExtensionsOfCommand(String commandName)
	{
		return commandExtensions.get(commandName);
	}

}
