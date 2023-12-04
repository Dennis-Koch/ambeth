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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.HashMap;

import java.util.Collection;

public class CommandBuilder implements ICommandBuilder {
    protected final HashMap<Class<?>, ICommandCreator> commandTypeToCreatorMap = new HashMap<>();
    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected ICommandBuilder commandBuilder;
    @Autowired
    protected IObjRefHelper objRefHelper;

    public CommandBuilder() {
        commandTypeToCreatorMap.put(ResolveObjectCommand.class, (commandTypeRegistry, objectFuture, parent, optionals) -> new ResolveObjectCommand(objectFuture));
        commandTypeToCreatorMap.put(ArraySetterCommand.class,
                (commandTypeRegistry, objectFuture, parent, optionals) -> new ArraySetterCommand(objectFuture, parent, ((Number) optionals[0]).intValue()));
        commandTypeToCreatorMap.put(CollectionSetterCommand.class,
                (commandTypeRegistry, objectFuture, parent, optionals) -> new CollectionSetterCommand(objectFuture, parent, optionals.length == 1 ? optionals[0] : null));
        commandTypeToCreatorMap.put(MergeCommand.class, (commandTypeRegistry, objectFuture, parent, optionals) -> new MergeCommand(objectFuture, parent, commandBuilder, objRefHelper));
        commandTypeToCreatorMap.put(ObjectSetterCommand.class, (commandTypeRegistry, objectFuture, parent, optionals) -> new ObjectSetterCommand(objectFuture, parent, (Member) optionals[0]));
    }

    @Override
    public IObjectCommand build(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture, Object parent, Object... optionals) {
        if (parent == null) {
            return buildIntern(commandTypeRegistry, ResolveObjectCommand.class, objectFuture, parent, optionals);
        } else if (parent.getClass().isArray()) {
            return buildIntern(commandTypeRegistry, ArraySetterCommand.class, objectFuture, parent, optionals);
        } else if (parent instanceof Collection) {
            return buildIntern(commandTypeRegistry, CollectionSetterCommand.class, objectFuture, parent, optionals);
        } else if (parent instanceof CreateContainer || parent instanceof UpdateContainer) {
            return buildIntern(commandTypeRegistry, MergeCommand.class, objectFuture, parent, optionals);
        }
        return buildIntern(commandTypeRegistry, ObjectSetterCommand.class, objectFuture, parent, optionals);
    }

    protected <C extends IObjectCommand> IObjectCommand buildIntern(ICommandTypeRegistry commandTypeRegistry, Class<? extends C> commandType, IObjectFuture objectFuture, Object parent,
            Object[] optionals) {
        var commandCreator = commandTypeRegistry.getOverridingCommandType(commandType);
        if (commandCreator == null) {
            commandCreator = commandTypeToCreatorMap.get(commandType);
        }
        return commandCreator.createCommand(commandTypeRegistry, objectFuture, parent, optionals);
    }
}
