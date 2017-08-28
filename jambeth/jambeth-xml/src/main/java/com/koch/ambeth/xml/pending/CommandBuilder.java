package com.koch.ambeth.xml.pending;

/*-
 * #%L
 * jambeth-xml
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

import java.util.Collection;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.UpdateContainer;

public class CommandBuilder implements ICommandBuilder {
	@Autowired
	protected IServiceContext beanContext;

	@Override
	public IObjectCommand build(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture,
			Object parent, Object... optionals) {
		IObjectCommand command;
		if (parent == null) {
			command = buildIntern(commandTypeRegistry, ResolveObjectCommand.class, objectFuture, null)
					.finish();
		}
		else if (parent.getClass().isArray()) {
			command = buildIntern(commandTypeRegistry, ArraySetterCommand.class, objectFuture, parent)
					.propertyValue("Index", optionals[0]).finish();
		}
		else if (parent instanceof Collection) {
			command = new CollectionSetterCommand(objectFuture, parent,
					optionals.length == 1 ? optionals[0] : null);
		}
		else if (parent instanceof CreateContainer || parent instanceof UpdateContainer) {
			command = buildIntern(commandTypeRegistry, MergeCommand.class, objectFuture, parent).finish();
		}
		else {
			command = buildIntern(commandTypeRegistry, ObjectSetterCommand.class, objectFuture, parent)
					.propertyValue("Member", optionals[0]).finish();
		}

		return command;
	}

	protected <C extends IObjectCommand> IBeanRuntime<? extends C> buildIntern(
			ICommandTypeRegistry commandTypeRegistry, Class<? extends C> commandType,
			IObjectFuture objectFuture, Object parent) {
		Class<? extends C> overridingCommandType = commandTypeRegistry
				.getOverridingCommandType(commandType);
		if (overridingCommandType != null) {
			commandType = overridingCommandType;
		}
		IBeanRuntime<? extends C> beanRuntime = beanContext.registerBean(commandType)
				.propertyValue("ObjectFuture", objectFuture).propertyValue("Parent", parent);
		return beanRuntime;
	}
}
