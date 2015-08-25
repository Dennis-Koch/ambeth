package de.osthus.ambeth.shell.core.command;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.shell.AmbethShell;

public abstract class AbstractCommandBundle
{
	// TODO change back to using shellContext directly
	// @Autowired
	// protected ShellContext shellContext;

	@Autowired
	protected AmbethShell shell;
}
