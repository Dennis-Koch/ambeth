package com.koch.ambeth.shell.core;

import java.util.List;

/**
 *
 * Binds methods to commands and generates help and usage info
 *
 * @author daniel.mueller
 *
 */
public interface CommandBinding
{
	/**
	 *
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	Object execute(List<String> arguments);

	/**
	 *
	 * @return
	 */
	String getName();

	/**
	 *
	 * @return
	 */
	String getDescription();

	/**
	 *
	 */
	String printHelp();

	/**
	 *
	 */
	String printUsage();
}
