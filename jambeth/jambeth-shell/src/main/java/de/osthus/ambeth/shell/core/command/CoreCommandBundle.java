package de.osthus.ambeth.shell.core.command;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.shell.core.CommandBinding;
import de.osthus.ambeth.shell.core.annotation.Command;
import de.osthus.ambeth.shell.core.annotation.CommandArg;
import de.osthus.ambeth.shell.core.resulttype.CommandResult;
import de.osthus.ambeth.shell.core.resulttype.ListResult;
import de.osthus.ambeth.shell.core.resulttype.SingleResult;
import de.osthus.ambeth.shell.util.Utils;

public class CoreCommandBundle extends AbstractCommandBundle
{

	@Command(name = "set", description = "set variables into the shell context")
	public CommandResult set(
			@CommandArg(alt = "variable=[value]", name = ".*", optional = true, //
			description = "stores variables in shell context. Use without arguments or without value to print entire context or the value of the variable to the console")//
			Entry<String, Object> entry)
	{

		if (entry != null)
		{
			if (entry.getValue() != null && entry.getKey() != null && !entry.getKey().isEmpty())
			{
				shell.getContext().set(entry.getKey(), entry.getValue());
				return null;
			}
			else
			{
				String key = String.valueOf(entry.getValue());
				String value = "" + shell.getContext().get(key);
				if (key.matches("(?i:.*PASS.*)"))
				{
					value = "**********";
				}
				ListResult<String> cmdRst = new ListResult<String>();
				cmdRst.addRecord(key + "=" + value);
				return cmdRst;
			}
		}
		else
		{

			IList<String> list = shell.getContext().getAll().keySet().toList();
			Collections.sort(list);
			ListResult<String> cmdRst = new ListResult<String>();
			for (String key : list)
			{
				cmdRst.addRecord(key + "=" + shell.getContext().get(key));
			}
			return cmdRst;
		}

	}

	@Command(name = "echo", description = "Prints messages to the console")
	public CommandResult echo(@CommandArg(name = "", alt = "message", description = "the message to print") Object value)
	{
		String filteredValue = shell.getContext().filter(value.toString());
		SingleResult cmdRst = new SingleResult("echo");
		cmdRst.addValue(filteredValue);
		return cmdRst;
	}

	@Command(name = "wait", description = "pauses execution for the given amount of milli seconds")
	public CommandResult wait(@CommandArg String millis)
	{
		long lMillis = Long.valueOf(millis);
		// shell.println("Waiting until " + new Date(System.currentTimeMillis() + lMillis));
		try
		{
			Thread.sleep(Long.valueOf(lMillis));
		}
		catch (InterruptedException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return null;
	}

	@Command(name = "exit", description = "exit the shell")
	public CommandResult exit(@CommandArg(optional = true, defaultValue = "0", alt = "exit-code", description = "the exit code") String exitStatus)
	{
		shell.exit(Integer.parseInt(exitStatus));
		return null;
	}

	@Command(name = "help", description = "list all commands, or print details for a specific command")
	public CommandResult printHelp(@CommandArg(alt = "command-name", optional = true, description = "The name of a command") String commandName)
	{
		SingleResult cmdRst = new SingleResult();
		if (commandName != null)
		{
			CommandBinding commandBinding = shell.getCommandBinding(commandName);
			if (commandBinding == null)
			{
				throw new RuntimeException("Unknown command: " + commandName);
			}
			cmdRst.setValue(commandBinding.printHelp());
		}
		else
		{
			SortedMap<String, String> help = new TreeMap<String, String>();
			int max = 0;
			for (CommandBinding cb : shell.getCommandBindings())
			{
				max = cb.getName().length() > max ? cb.getName().length() : max;
				help.put(cb.getName(), cb.getDescription());
			}

			String description;
			StringBuffer strBuf = new StringBuffer();
			int i = 0;
			for (String name : help.keySet())
			{
				description = help.get(name);
				strBuf.append(Utils.stringPadEnd(name, max, ' '));
				if (!description.isEmpty())
				{
					strBuf.append(" - " + description);
				}
				if (i < help.keySet().size() - 1)
				{
					strBuf.append(System.lineSeparator());
				}
				i++;
			}
			cmdRst.setValue(strBuf.toString());
		}
		return cmdRst;
	}

	public static String readableFileSize(long size)
	{
		if (size <= 0)
		{
			return "0B";
		}
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
