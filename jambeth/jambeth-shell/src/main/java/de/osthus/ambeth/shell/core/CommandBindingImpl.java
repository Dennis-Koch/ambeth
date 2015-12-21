package de.osthus.ambeth.shell.core;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import de.osthus.ambeth.shell.AmbethShell;
import de.osthus.ambeth.shell.core.CommandExtension.Parameter;
import de.osthus.ambeth.shell.core.CommandExtension.Usage;
import de.osthus.ambeth.shell.core.annotation.CommandArg;
import de.osthus.ambeth.shell.util.Utils;

/**
 * {@inheritDoc}
 */
public class CommandBindingImpl implements CommandBinding
{

	@Property
	protected String name;
	@Property
	protected String description;
	@Property
	protected Object commandBean;
	@Property
	protected List<CommandArg> args;
	@Property
	protected int methodIndex;
	@Property
	protected Class<?>[] parameterTypes;
	@Property
	protected MethodAccess methodAccess;

	@Autowired
	protected AmbethShell shell;
	@Autowired
	protected CommandExtensionExtendable commandExtensions;

	@Override
	public Object execute(List<String> arguments)
	{
		Object[] translatedArgs = new Object[parameterTypes.length];

		Set<ParsedArgument> parsedArgs = new HashSet<ParsedArgument>();
		for (int n = 0; n < arguments.size(); n++)
		{
			parsedArgs.add(new ParsedArgument(arguments.get(n), n, shell.getContext()));
		}

		int index = 0;
		for (CommandArg ca : args)
		{
			Class<?> paramType = parameterTypes[index];
			for (ParsedArgument pa : parsedArgs)
			{
				if (pa.matchedBy(ca))
				{
					if (Entry.class.equals(paramType))
					{
						translatedArgs[index] = new SimpleEntry<String, Object>(pa.name, pa.value);
					}
					else
					{
						translatedArgs[index] = pa.value;
					}
				}
			}
			if (translatedArgs[index] == null && !ca.defaultValue().isEmpty())
			{
				translatedArgs[index] = ca.defaultValue();
			}
			if (translatedArgs[index] == null && !ca.optional())
			{
				printUsage();
				throw new RuntimeException("Mandatory argument is missing!");
			}
			index++;
		}

		try
		{
			return methodAccess.invoke(commandBean, methodIndex, translatedArgs);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void printHelp()
	{
		if (!description.isEmpty())
		{
			shell.println(description);
		}
		printUsage();
	}

	/**
	 * http://courses.cms.caltech.edu/cs11/material/general/usage.html
	 */
	@Override
	public void printUsage()
	{
		String indent = "  ";

		shell.println("Usage:");
		shell.print(indent + name + " ");
		int maxArgStringLength = 0;
		for (CommandArg arg : args)
		{
			String argName = arg.name().isEmpty() ? "<" + arg.alt() + ">" : arg.name();
			// String argName = arg.name();

			// argName += arg.defaultValue();

			argName = !argName.isEmpty() && arg.optional() ? "[" + argName + "]" : argName;

			shell.print(argName + " ");
			maxArgStringLength = Math.max(maxArgStringLength, Math.max(argName.length(), arg.alt().length() + 2));
		}
		shell.println();

		if (args.size() > 0)
		{

			shell.println("options:");
			for (CommandArg arg : args)
			{
				String argName = arg.name().isEmpty() ? "<" + arg.alt() + ">" : arg.name();
				argName = !argName.isEmpty() && arg.optional() ? "[" + argName + "]" : argName;
				shell.print(indent + Utils.stringPadEnd(argName, maxArgStringLength + 4, ' '));
				String description = arg.description();
				if (!arg.descriptionFile().isEmpty())
				{
					// TODO implement read descriptions from files
					try
					{
						description = Utils.readInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(arg.descriptionFile()));
					}
					catch (IOException e)
					{
						shell.println("Warning: Failure to load description file: " + arg.descriptionFile());
					}
				}
				shell.print(description);
				if (!arg.defaultValue().isEmpty())
				{
					shell.print(" [DEFAULT: " + arg.defaultValue() + "]");
				}
				shell.println();
			}
		}
		// print addition info from extensions
		printExtensionUsages(commandExtensions.getExtensionsOfCommand(name));
	}

	/**
	 * print usage of command extensions
	 * 
	 * @param extensions
	 */
	private void printExtensionUsages(List<CommandExtension> extensions)
	{
		if (extensions == null || extensions.size() == 0)
		{
			return;
		}
		shell.println();

		List<Usage> usages = new ArrayList<Usage>();
		int maxParamLength = 0;
		for (CommandExtension extension : extensions)
		{
			Usage usage = extension.getUsage();
			if (usage == null)
			{
				continue;
			}
			usages.add(usage);

			if (usage.getParameters() != null)
			{
				for (Parameter p : usage.getParameters())
				{
					maxParamLength = Math.max(p.getName().length(), maxParamLength);
				}
			}

		}
		maxParamLength += 3;

		for (Usage usage : usages)
		{

			shell.println("Extension:   " + usage.getName());
			shell.println("Description: " + usage.getDescription());
			if (usage.getParameters() != null && usage.getParameters().size() > 0)
			{
				shell.println("Parameters:");
				for (Parameter p : usage.getParameters())
				{
					shell.print("  " + Utils.stringPadEnd("[" + p.getName() + "]", maxParamLength, ' '));
					shell.print(p.getDescription());
					if (p.getDefaultValue() != null)
					{
						shell.print(" [Default: " + p.getDefaultValue() + "]");
					}
					shell.println();
				}
			}
			shell.println();
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

}
