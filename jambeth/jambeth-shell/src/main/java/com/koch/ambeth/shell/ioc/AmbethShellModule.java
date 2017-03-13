package com.koch.ambeth.shell.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.PrecedenceType;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.shell.AmbethShell;
import com.koch.ambeth.shell.AmbethShellStarter;
import com.koch.ambeth.shell.core.AmbethShellImpl;
import com.koch.ambeth.shell.core.AmbethShellIntern;
import com.koch.ambeth.shell.core.CommandBindingExtendable;
import com.koch.ambeth.shell.core.CommandExtensionExtendable;
import com.koch.ambeth.shell.core.CommandExtensionImpl;
import com.koch.ambeth.shell.core.ShellContext;
import com.koch.ambeth.shell.core.ShellContextImpl;
import com.koch.ambeth.shell.core.command.CoreCommandModule;

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
