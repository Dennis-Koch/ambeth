package de.osthus.ambeth.shell.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.shell.AmbethShell;
import de.osthus.ambeth.shell.AmbethShellStarter;
import de.osthus.ambeth.shell.core.AmbethShellImpl;
import de.osthus.ambeth.shell.core.AmbethShellIntern;
import de.osthus.ambeth.shell.core.CommandBindingExtendable;
import de.osthus.ambeth.shell.core.CommandExtensionExtendable;
import de.osthus.ambeth.shell.core.CommandExtensionImpl;
import de.osthus.ambeth.shell.core.ShellContext;
import de.osthus.ambeth.shell.core.ShellContextImpl;
import de.osthus.ambeth.shell.core.command.CoreCommandModule;

public class AmbethShellModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
	{
		bcf.registerBean(CoreCommandModule.class);

		bcf.registerBean(AmbethShellImpl.class).autowireable(AmbethShell.class, AmbethShellIntern.class, CommandBindingExtendable.class);
		bcf.registerBean(AmbethShellStarter.class).autowireable(AmbethShellStarter.class).precedence(PrecedenceType.LOWEST);
		bcf.registerBean(CommandScanner.class);
		bcf.registerBean(ShellContextImpl.class).autowireable(ShellContext.class);
		bcf.registerBean(CommandExtensionImpl.class).autowireable(CommandExtensionExtendable.class);
	}
}
