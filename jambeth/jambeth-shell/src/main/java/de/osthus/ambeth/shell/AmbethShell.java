package de.osthus.ambeth.shell;

import java.io.BufferedReader;
import java.text.DateFormat;
import java.util.Collection;

import de.osthus.ambeth.shell.core.CommandBinding;
import de.osthus.ambeth.shell.core.ShellContext;

/**
 *
 * @author daniel.mueller
 *
 */
public interface AmbethShell
{
	public static final String PROPERTY_SHELL_MODE = "SHELL_MODE";
	public static final String MODE_INTERACTIVE = "INTERACTIVE";
	public static final String MODE_SERVICE = "SERVICE";

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
	 */
	void executeCommand(String... args);

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
	 * @param object
	 */
	void exit(int status);

	/**
	 * all commands should use the same Date format
	 * 
	 * @return
	 */
	DateFormat getDateFormat();
}
