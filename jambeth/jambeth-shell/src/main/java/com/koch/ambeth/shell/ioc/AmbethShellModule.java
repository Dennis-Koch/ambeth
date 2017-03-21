package com.koch.ambeth.shell.ioc;

/*-
 * #%L
 * jambeth-shell
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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

public class AmbethShellModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable {
		bcf.registerBean(CoreCommandModule.class);

		bcf.registerBean(AmbethShellImpl.class).autowireable(AmbethShell.class, AmbethShellIntern.class,
				CommandBindingExtendable.class);
		bcf.registerBean(AmbethShellStarter.class).autowireable(AmbethShellStarter.class)
				.precedence(PrecedenceType.LOWEST);
		bcf.registerBean(CommandScanner.class);
		bcf.registerBean(ShellContextImpl.class).autowireable(ShellContext.class);
		bcf.registerBean(CommandExtensionImpl.class).autowireable(CommandExtensionExtendable.class);
	}
}
