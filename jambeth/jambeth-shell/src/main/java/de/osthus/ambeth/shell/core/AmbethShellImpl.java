package de.osthus.ambeth.shell.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.Closer;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.format.ISO8601DateFormat;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.shell.AmbethShell;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.IConversionHelper;

public class AmbethShellImpl implements AmbethShell, AmbethShellIntern, CommandBindingExtendable, IThreadLocalCleanupBean
{
	private static final boolean HIDE_IO_DEFAULT = true;
	private static final boolean EXIT_ON_ERROR_DEFAULT = false;
	private static final boolean VERBOSE_DEFAULT = false;
	private static final boolean ECHO_DEFAULT = false;

	private static final String HIDE_IO = "hide.io";
	private static final String PROMPT_SIGN = ">";
	private static final String DEFAULT_PROMPT = "AMBETH";
	private static final String ECHO = "echo";

	private static final Pattern versionExtractPattern = Pattern.compile("(\\d+\\.\\d+).*");

	@LogInstance
	private ILogger log;

	protected final ThreadLocal<DateFormat> isoDateFormatTL = new SensitiveThreadLocal<DateFormat>();

	protected final Map<String, CommandBinding> commandBindings = new HashMap<String, CommandBinding>();

	@Autowired
	protected ShellContext context;

	@Autowired
	protected IConversionHelper conversionHelper;

	protected PrintStream shellOut = System.out;

	@Property(name = ShellContext.BATCH_FILE, mandatory = false, defaultValue = "")
	protected String batchFile;

	@Property(name = ShellContext.MAIN_ARGS, mandatory = false)
	protected String[] mainArgs;

	/**
	 * 
	 * @return
	 */
	private static final DateFormat createIsoDateFormat()
	{

		SimpleDateFormat.getDateInstance().format(new Date());

		String versionProperty = System.getProperty("java.version");
		Matcher versionMatcher = versionExtractPattern.matcher(versionProperty);
		if (versionMatcher.find() && Double.parseDouble(versionMatcher.group(1)) >= 1.7)
		{
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		else
		{
			return new ISO8601DateFormat();
		}
	}

	public void startShell()
	{
		initSystemIO();
		AmbethShellImpl.this.preventSystemOut();

		if (batchFile != null && !batchFile.isEmpty())
		{
			try
			{
				Closer closer = Closer.create();
				try
				{
					BufferedReader scriptReader = closer.register(new BufferedReader(new FileReader(new File(batchFile))));
					this.getContext().set(ECHO, true);
					this.startInteractive(scriptReader);
				}
				catch (Throwable e)
				{
					throw closer.rethrow(e);
				}
				finally
				{
					closer.close();
				}
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else if (mainArgs != null && mainArgs.length > 0)
		{
			try
			{
				this.executeCommand(mainArgs);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			this.startInteractive(new BufferedReader(new InputStreamReader(System.in)));
		}
	}

	private void initSystemIO()
	{
		shellOut = System.out;
	}

	private void preventSystemOut()
	{
		PrintStream noOpSystemOut = new PrintStream(new OutputStream()
		{
			@Override
			public void write(int b) throws IOException
			{
				if (!getContext().get(HIDE_IO, HIDE_IO_DEFAULT))
				{
					shellOut.write(b);
				}
			}
		});

		System.setOut(noOpSystemOut);
		System.setErr(noOpSystemOut);
	}

	@Override
	public void startInteractive(BufferedReader br)
	{
		try
		{
			String userInput;
			while (true)
			{
				prompt();
				userInput = br.readLine();
				if (userInput == null)
				{
					break;
				}

				if (getContext().get(ECHO, ECHO_DEFAULT))
				{
					println(userInput);
				}

				try
				{
					List<String> parts = parseUserInput(userInput);
					executeCommand(parts.toArray(new String[parts.size()]));
				}
				catch (Exception e)
				{
					handleCommandError(e);
				}
			}
		}
		catch (IOException e)
		{
			log.error("failed to handle IO!");
		}
	}

	/**
	 * // TODO find a regex Guru :)
	 * 
	 * @param userInput
	 * @return
	 */
	private List<String> parseUserInput(String userInput)
	{
		List<String> parts = new ArrayList<String>();
		String sequence = "";
		boolean insideQuotes = false;
		boolean whitespaceRegion = false;
		for (int i = 0; i < userInput.length(); i++)
		{
			String c = "" + userInput.charAt(i);
			if (c.matches("\\s") && !insideQuotes)
			{
				whitespaceRegion = true;
				continue;
			}

			if (c.matches("\""))
			{
				if (insideQuotes)
				{
					// closing quotes
					insideQuotes = false;
				}
				else
				{
					// starting quotes
					insideQuotes = true;
				}
			}

			if (whitespaceRegion)
			{
				// end of WS region
				parts.add(sequence);
				sequence = "";
				whitespaceRegion = false;
			}
			sequence += c;
		}
		parts.add(sequence);
		return parts;
	}

	@Override
	public CommandBinding getCommandBinding(String name)
	{
		return commandBindings.get(name);
	}

	@Override
	public Collection<CommandBinding> getCommandBindings()
	{
		return commandBindings.values();
	}

	/**
	 * examples: set foo=bar echo "hello world" open test.adf
	 */
	@Override
	public void executeCommand(String... args)
	{
		if (args != null && args.length > 0)
		{
			String commandName = args[0].trim();
			if (commandName.length() == 0)
			{
				return;
			}
			List<String> argSet = new ArrayList<String>();
			for (int i = 1; i < args.length; i++)
			{
				argSet.add(args[i]);
			}
			CommandBinding command = commandBindings.get(commandName.toLowerCase());
			if (command == null)
			{
				println("Unknown command: " + commandName);
				return;
			}
			// FIXME use conversion helper!!!
			command.execute(argSet);
		}
	}

	/**
	 *
	 * @param cb
	 * @param unparsedArgs
	 * @param e
	 */
	private void handleCommandError(Exception e)
	{
		if (context.get(ShellContext.VERBOSE, VERBOSE_DEFAULT))
		{
			e.printStackTrace(shellOut);
		}
		else
		{
			String message = e.getMessage();
			if (message == null && e instanceof InvocationTargetException)
			{
				message = ((InvocationTargetException) e).getTargetException().getMessage();
			}
			else if (message == null && e.getCause() != null)
			{
				message = e.getCause().getMessage();
			}
			println(message);
		}

		if (context.get(ShellContext.EXIT_ON_ERROR, EXIT_ON_ERROR_DEFAULT))
		{
			exit(1);
		}
	}

	private void prompt()
	{
		String promptValue = context.get(ShellContext.PROMPT, DEFAULT_PROMPT);
		print(promptValue + PROMPT_SIGN);
	}

	@Override
	public void print(Object object)
	{
		shellOut.print(object);
	}

	@Override
	public void println()
	{
		shellOut.println();
	}

	@Override
	public void println(Object object)
	{
		shellOut.println(object);
	}

	@Override
	public void exit(int status)
	{
		// this is OK because our IoC context is bound to the VM shutdown hook for a graceful shutdown procedure
		System.exit(status);
	}

	@Override
	public void register(CommandBinding commandBinding, String commandName)
	{
		commandBindings.put(commandName, commandBinding);
	}

	@Override
	public void unregister(CommandBinding commandBinding, String commandName)
	{
		commandBindings.remove(commandName);
	}

	@Override
	public ShellContext getContext()
	{
		return context;
	}

	public void setContext(ShellContext shellContext)
	{
		this.context = shellContext;
	}

	public DateFormat getDateFormat()
	{
		DateFormat dateFormat = isoDateFormatTL.get();
		if (dateFormat == null)
		{
			dateFormat = createIsoDateFormat();
			isoDateFormatTL.set(dateFormat);
		}
		return dateFormat;
	}

	@Override
	public void cleanupThreadLocal()
	{
		isoDateFormatTL.set(null);
	}

}
