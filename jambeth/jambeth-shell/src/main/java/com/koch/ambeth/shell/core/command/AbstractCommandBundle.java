package com.koch.ambeth.shell.core.command;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.shell.AmbethShell;

public abstract class AbstractCommandBundle
{
	// TODO change back to using shellContext directly
	// @Autowired
	// protected ShellContext shellContext;

	@Autowired
	protected AmbethShell shell;
}
