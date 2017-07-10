package com.koch.ambeth.shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.core.start.IAmbethConfiguration;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.shell.core.AmbethShellIntern;
import com.koch.ambeth.shell.core.License;
import com.koch.ambeth.shell.core.ShellContext;
import com.koch.ambeth.shell.ioc.AmbethShellModule;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class AmbethShellStarter implements IDisposableBean {
	@Autowired
	protected AmbethShellIntern ambethShell;

	protected boolean destroyed;

	protected Thread shellHandler;

	@Property(name = AmbethShell.PROPERTY_SHELL_MODE, defaultValue = AmbethShell.MODE_INTERACTIVE, mandatory = true)
	protected String mode;

	/**
	 * start the shell
	 *
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		System.setProperty(ShellContext.GREETING_ACTIVE, "false");
		System.setProperty(ShellContext.LICENSE_TYPE, License.COMMERCIAL.toString());
		// System.setProperty(ShellContext.LICENSE_TEXT, "Evaluation license for non-commercial use
		// only!");
		System.setProperty(ShellContext.PRODUCT_NAME, "Ambeth Shell");
		System.setProperty(ShellContext.PRODUCT_VERSION, "1.2.3");

		// Calendar c = Calendar.getInstance();
		// c.set(2099, 0, 1);
		// System.setProperty(ShellContext.LICENSE_EXPIRATION_DATE, new
		// Long(c.getTimeInMillis()).toString());
		// start(args, "com/koch/ambeth/shell/.*");

		System.setProperty(CoreConfigurationConstants.PackageScanPatterns, "com/koch/ambeth/shell/.*");
		start(args);
	}

	@SuppressWarnings("unchecked")
	public static void start(String[] args, Class<? extends IInitializingModule>... modules) {
		Properties parsedArgs = parseMainArgs(args);

		IAmbethConfiguration appConfig = Ambeth.createDefault()//
				.withProperty("ambeth.log.level.com.koch.ambeth", "INFO")//
				.withProperty(IocConfigurationConstants.DebugModeActive, "true")//
				.withProperty(IocConfigurationConstants.TrackDeclarationTrace, "true")//
				.withApplicationModules(AmbethShellModule.class)//
				.withApplicationModules(modules)//
				.withProperties(parsedArgs)//
				.withoutPropertiesFileSearch();//
		startRunClose(appConfig);
	}

	@SuppressWarnings("unchecked")
	public static void start(String[] args, Properties properties,
			Class<? extends IInitializingModule>... modules) {
		Properties parsedArgs = parseMainArgs(args);

		IAmbethConfiguration appConfig = Ambeth.createDefault()//
				.withProperty("ambeth.log.level.com.koch.ambeth", "INFO")//
				.withProperty(IocConfigurationConstants.DebugModeActive, "true")//
				.withProperty(IocConfigurationConstants.TrackDeclarationTrace, "true")//
				.withProperties(properties) //
				.withApplicationModules(AmbethShellModule.class)//
				.withApplicationModules(modules)//
				.withProperties(parsedArgs)//
				.withoutPropertiesFileSearch();//

		startRunClose(appConfig);
	}

	private static void startRunClose(IAmbethConfiguration appConfig) {
		IAmbethApplication app = null;
		try {
			app = appConfig.start();
			IServiceContext serviceContext = app.getApplicationContext();
			AmbethShellStarter starter = serviceContext.getService(AmbethShellStarter.class);
			starter.run();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			if (app != null) {
				try {
					app.close();
				}
				catch (Exception e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
	}

	/**
	 *
	 * we cannot just let Ambeth absorb these properties because they have no name or key, e.g. java
	 * -jar shell.jar myscript.as or java -jar create test.adf
	 *
	 *
	 *
	 *
	 * @param args
	 * @return
	 */
	private static Properties parseMainArgs(String[] args) {
		Properties mainProperties = new Properties();
		List<String> cleanedMainArgs = new ArrayList<>();

		for (String arg : args) {
			if (arg.toLowerCase().endsWith(".as")) {
				if (!validateBatchFileVariables(mainProperties, arg, args)) {
					throw new IllegalArgumentException(
							"Variable settings are illegal. (required format: key=value) ");
				}
				mainProperties.put(ShellContext.BATCH_FILE, arg);
			}
			else {
				cleanedMainArgs.add(arg);
			}
		}

		if (!cleanedMainArgs.isEmpty()) {
			// to be able to find the originally passed main args, we have to set them
			mainProperties.put(ShellContext.MAIN_ARGS,
					cleanedMainArgs.toArray(new String[cleanedMainArgs.size()]));
		}
		return mainProperties;
	}

	/**
	 * validate the variable setting inputs for the batch file(.as file) and save them to Properties
	 * if all are valid.
	 *
	 * @param properties
	 *          {@link Properties} that be used for starting Ambeth
	 * @param batchFile
	 *          batch file name (.as file)
	 * @param args
	 *          all the arguments inputed from the program starting
	 * @return <code> true</code> if all the variables are correct. <code>false</code> if any variable
	 *         is not correct
	 */
	private static boolean validateBatchFileVariables(Properties properties, String batchFile,
			String... args) {
		if (!batchFile.equals(args[0])) {
			return false;
		}

		HashMap<String, String> varMap = new HashMap<>();

		for (int i = 1; i < args.length; i++) {
			String[] varPairs = args[i].split("=");
			// variable input need to be in the format of "key=value"
			if (varPairs.length != 2 || "".equals(varPairs[0].trim()) || "".equals(varPairs[1].trim())) {
				return false;
			}
			else {
				varMap.put(varPairs[0], varPairs[1]);
			}
		}

		// save the variables map to properties
		properties.put(ShellContext.VARS_FOR_BATCH_FILE, varMap);

		return true;
	}

	@Override
	public void destroy() throws Throwable {
		destroyed = true;
		if (shellHandler != null) {
			shellHandler.interrupt();
		}
	}

	public void run() throws Exception {
		if (AmbethShell.MODE_SERVICE.equals(mode)) {
			return;
		}
		// FIXME: AF deactivated this, because we now have a livespan fo the ambeth context see
		// startRunClose.
		boolean useThread = false;
		if (useThread) {
			shellHandler = new Thread(new Runnable() {
				@Override
				public void run() {
					ambethShell.startShell();
				}
			}, "AdfShell-Handler");
			shellHandler.start();
		}
		else {
			ambethShell.startShell();
		}
	}
}
