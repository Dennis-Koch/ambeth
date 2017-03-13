package com.koch.ambeth.shell.core;

public interface CommandBindingExtendable
{
	/**
	 *
	 * @param commandBinding
	 * @param commandName
	 */
	void register(CommandBinding commandBinding, String commandName);

	/**
	 *
	 * @param arg0
	 * @param arg1
	 */
	void unregister(CommandBinding commandBinding, String commandName);
}
