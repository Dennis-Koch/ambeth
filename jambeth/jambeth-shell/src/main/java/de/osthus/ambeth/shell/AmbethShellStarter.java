package de.osthus.ambeth.shell;

import java.util.ArrayList;
import java.util.List;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.shell.core.AmbethShellIntern;
import de.osthus.ambeth.shell.core.ShellContext;
import de.osthus.ambeth.shell.ioc.AmbethShellModule;

public class AmbethShellStarter implements IStartingBean, IDisposableBean
{
	@Autowired
	protected AmbethShellIntern adfShell;

	protected boolean destroyed;

	protected Thread shellHandler;

	/**
	 * start the shell
	 * 
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		start(args);
	}

	public static void start(String[] args, Class<? extends IInitializingModule>... modules)
	{
		Properties parsedArgs = parseMainArgs(args);

		Ambeth.createDefault()//
				.withProperty(CoreConfigurationConstants.PackageScanPatterns, "N/A")//
				.withProperty("ambeth.log.level.de.osthus.ambeth", "INFO")//
				.withProperty(IocConfigurationConstants.DebugModeActive, "true")//
				.withProperty(IocConfigurationConstants.TrackDeclarationTrace, "true")//
				.withApplicationModules(AmbethShellModule.class)//
				.withApplicationModules(modules)//
				.withProperties(parsedArgs)//
				.withoutPropertiesFileSearch()//
				.startAndClose();
	}

	/**
	 * 
	 * we cannot just let Ambeth absorb these properties because they have no name or key, e.g. java -jar shell.jar myscript.as or java -jar create test.adf
	 * 
	 * 
	 * @param args
	 * @return
	 */
	private static Properties parseMainArgs(String[] args)
	{
		Properties mainProperties = new Properties();
		List<String> cleanedMainArgs = new ArrayList<String>();

		for (String arg : args)
		{
			if (arg.toLowerCase().endsWith(".as"))
			{
				mainProperties.put(ShellContext.BATCH_FILE, arg);
			}
			else
			{
				cleanedMainArgs.add(arg);
			}
		}

		if (!cleanedMainArgs.isEmpty())
		{
			// to be able to find the originally passed main args, we have to set them
			mainProperties.put(ShellContext.MAIN_ARGS, cleanedMainArgs.toArray(new String[cleanedMainArgs.size()]));
		}
		return mainProperties;
	}

	@Override
	public void destroy() throws Throwable
	{
		destroyed = true;
		if (shellHandler != null)
		{
			shellHandler.interrupt();
		}
	}

	@Override
	public void afterStarted() throws Throwable
	{
		boolean useThread = true;
		if (useThread)
		{
			shellHandler = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					adfShell.startShell();
				}
			}, "AdfShell-Handler");
			shellHandler.start();
		}
		else
		{
			adfShell.startShell();
		}
	}
}
