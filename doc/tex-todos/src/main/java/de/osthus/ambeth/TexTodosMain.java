package de.osthus.ambeth;

import de.osthus.ambeth.bundle.Core;

public class TexTodosMain
{
	public static void main(String[] args)
	{
		Ambeth.createEmptyBundle(Core.class).withApplicationModules(TexTodosModule.class).withArgs(args).startAndClose();
	}
}
