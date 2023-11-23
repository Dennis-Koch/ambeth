package com.koch.ambeth.xml.postprocess.merge;

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
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IMergeController;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.MergeHandle;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import com.koch.ambeth.xml.pending.ArraySetterCommand;
import com.koch.ambeth.xml.pending.ICommandBuilder;
import com.koch.ambeth.xml.pending.ICommandCreator;
import com.koch.ambeth.xml.pending.ICommandTypeExtendable;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;
import com.koch.ambeth.xml.pending.ObjRefFuture;
import com.koch.ambeth.xml.postprocess.IPostProcessReader;
import com.koch.ambeth.xml.postprocess.IPostProcessWriter;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessor;

import java.util.List;

public class MergeXmlPostProcessor implements IXmlPostProcessor, IStartingBean {
    protected final ICommandCreator mergeArraySetterCommand = new ICommandCreator() {
        @Override
        public IObjectCommand createCommand(ICommandTypeRegistry commandTypeRegistry, IObjectFuture objectFuture, Object parent, Object[] optionals) {
            return new MergeArraySetterCommand(objectFuture, parent, ((Number) optionals[0]).intValue());
        }
    };
    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected ICacheFactory cacheFactory;
    @Autowired
    protected ICommandBuilder commandBuilder;
    @Autowired
    protected IMergeController mergeController;
    @Autowired
    protected IMemberTypeProvider memberTypeProvider;
    @Autowired
    protected IObjRefHelper objRefHelper;
    @Autowired
    protected ITypeInfoProvider typeInfoProvider;
    protected Member directObjRefDirectMember;

    @Override
    public void afterStarted() throws Throwable {
        directObjRefDirectMember = memberTypeProvider.getMember(DirectObjRef.class, "Direct");
    }

    @Override
    public Object processWrite(IPostProcessWriter writer) {
        var substitutedEntities = writer.getSubstitutedEntities();
        if (substitutedEntities.isEmpty()) {
            return null;
        }

        var childCache = cacheFactory.create(CacheFactoryDirective.NoDCE, "XmlMerge");
        var mergeContext =
                beanContext.createService("mergeXml", childContextFactory -> childContextFactory.registerAutowireableBean(MergeHandle.class, MergeHandle.class).propertyValue("Cache", childCache));
        try {
            var mutableToIdMap = writer.getMutableToIdMap();
            var objRefHelper = this.objRefHelper;
            var mergeHandle = mergeContext.getService(MergeHandle.class);
            for (var entity : substitutedEntities) {
                var ori = objRefHelper.entityToObjRef(entity);
                mergeHandle.getObjToOriDict().put(entity, ori);
                var id = mutableToIdMap.get(entity);
                mutableToIdMap.put(ori, id);
            }
            var cudResult = mergeController.mergeDeep(substitutedEntities, mergeHandle);
            if (cudResult.getAllChanges().isEmpty()) {
                return null;
            }
            return cudResult;
        } finally {
            mergeContext.dispose();
        }
    }

    @Override
    public void processRead(IPostProcessReader reader) {
        reader.nextTag();

        ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
        ICommandTypeExtendable commandTypeExtendable = reader.getCommandTypeExtendable();
        commandTypeExtendable.registerOverridingCommandCreator(mergeArraySetterCommand, ArraySetterCommand.class);
        Object result = reader.readObject();
        commandTypeExtendable.unregisterOverridingCommandCreator(mergeArraySetterCommand, ArraySetterCommand.class);

        if (!(result instanceof CUDResult)) {
            throw new IllegalArgumentException("Can only handle results of type '" + CUDResult.class.getName() + "'. Result of type '" + result.getClass().getName() + "' given.");
        }

        ICommandBuilder commandBuilder = this.commandBuilder;
        CUDResult cudResult = (CUDResult) result;
        List<IChangeContainer> changes = cudResult.getAllChanges();
        for (int i = 0, size = changes.size(); i < size; i++) {
            IChangeContainer changeContainer = changes.get(i);
            if (!(changeContainer instanceof CreateContainer)) {
                continue;
            }

            IObjRef ori = changeContainer.getReference();
            if (ori == null) {
                continue;
            } else if (ori instanceof IDirectObjRef) {
                IObjectFuture objectFuture = new ObjRefFuture(ori);
                IObjectCommand setterCommand = commandBuilder.build(commandTypeRegistry, objectFuture, ori, directObjRefDirectMember);
                reader.addObjectCommand(setterCommand);
                IObjectCommand mergeCommand = commandBuilder.build(commandTypeRegistry, objectFuture, changeContainer);
                reader.addObjectCommand(mergeCommand);
            } else {
                throw new IllegalStateException("Not implemented yet");
            }
        }
    }
}
