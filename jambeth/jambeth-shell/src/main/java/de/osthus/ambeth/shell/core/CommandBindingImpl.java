package de.osthus.ambeth.shell.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
				shell.println(printUsage());
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
	public String printHelp()
	{
		StringWriter strWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(strWriter);
		try
		{
			if (!description.isEmpty())
			{
				pw.println(description);
			}

			pw.print(printUsage());

			return strWriter.toString();
		}
		finally
		{
			pw.close();
			try
			{
				strWriter.close();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}

	}

	/**
	 * http://courses.cms.caltech.edu/cs11/material/general/usage.html
	 */
	@Override
	public String printUsage()
	{
		StringWriter strWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(strWriter);
		try
		{
			String indent = "  ";

			pw.println("Usage:");
			pw.print(indent + name + " ");
			int maxArgStringLength = 0;
			for (CommandArg arg : args)
			{
				String argName = arg.name().isEmpty() ? "<" + arg.alt() + ">" : arg.name();
				// String argName = arg.name();

				// argName += arg.defaultValue();

				argName = !argName.isEmpty() && arg.optional() ? "[" + argName + "]" : argName;

				pw.print(argName + " ");
				maxArgStringLength = Math.max(maxArgStringLength, Math.max(argName.length(), arg.alt().length() + 2));
			}
			pw.println();
			if (args.size() > 0)
			{
				pw.println("options:");
				for (CommandArg arg : args)
				{
					String argName = arg.name().isEmpty() ? "<" + arg.alt() + ">" : arg.name();
					argName = !argName.isEmpty() && arg.optional() ? "[" + argName + "]" : argName;
					pw.print(indent + Utils.stringPadEnd(argName, maxArgStringLength + 4, ' '));
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
							pw.println("Warning: Failure to load description file: " + arg.descriptionFile());
						}
					}
					pw.print(description);
					if (!arg.defaultValue().isEmpty())
					{
						pw.print(" [DEFAULT: " + arg.defaultValue() + "]");
					}
					pw.println();
				}
			}
			// print addition info from extensions
			pw.print(printExtensionUsages(commandExtensions.getExtensionsOfCommand(name)));

			return strWriter.toString();
		}
		finally
		{
			pw.close();
			try
			{
				strWriter.close();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}

	}

	/**
	 * print usage of command extensions
	 *
	 * @param extensions
	 */
	private String printExtensionUsages(List<CommandExtension> extensions)
	{
		if (extensions == null || extensions.size() == 0)
		{
			return "";
		}
		StringWriter strWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(strWriter);
		try
		{
			pw.println();

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

				pw.println("Extension:   " + usage.getName());
				pw.println("Description: " + usage.getDescription());
				if (usage.getParameters() != null && usage.getParameters().size() > 0)
				{
					pw.println("Parameters:");
					for (Parameter p : usage.getParameters())
					{
						pw.print("  " + Utils.stringPadEnd("[" + p.getName() + "]", maxParamLength, ' '));
						pw.print(p.getDescription());
						if (p.getDefaultValue() != null)
						{
							pw.print(" [Default: " + p.getDefaultValue() + "]");
						}
						pw.println();
					}
				}
				pw.println();
			}
			return strWriter.toString();
		}
		finally
		{
			pw.close();
			try
			{
				strWriter.close();
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
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
