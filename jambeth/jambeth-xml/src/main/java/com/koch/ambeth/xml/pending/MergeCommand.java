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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.OptimisticLockUtil;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.xml.IReader;
import lombok.SneakyThrows;

public class MergeCommand extends AbstractObjectCommand implements IObjectCommand, IInitializingBean {
    protected final ICommandBuilder commandBuilder;

    protected final IObjRefHelper objRefHelper;

    @SneakyThrows
    public MergeCommand(IObjectFuture objectFuture, Object parent, ICommandBuilder commandBuilder, IObjRefHelper objRefHelper) {
        super(objectFuture, parent);
        this.commandBuilder = commandBuilder;
        this.objRefHelper = objRefHelper;
        afterPropertiesSet();
    }

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        ParamChecker.assertParamOfType(parent, "Parent", IChangeContainer.class);
    }

    @Override
    public void execute(IReader reader) {
        IPrimitiveUpdateItem[] puis;
        IRelationUpdateItem[] ruis;
        if (parent instanceof CreateContainer) {
            CreateContainer createContainer = (CreateContainer) parent;
            puis = createContainer.getPrimitives();
            ruis = createContainer.getRelations();
        } else if (parent instanceof UpdateContainer) {
            UpdateContainer updateContainer = (UpdateContainer) parent;
            puis = updateContainer.getPrimitives();
            ruis = updateContainer.getRelations();
        } else {
            throw new IllegalArgumentException("Unsupported " + IChangeContainer.class.getSimpleName() + " of type '" + parent.getClass().getName() + "'");
        }

        var entity = objectFuture.getValue();
        var metadata = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
        applyPrimitiveUpdateItems(entity, puis, metadata);

        if (ruis != null && ruis.length > 0) {
            applyRelationUpdateItems((IObjRefContainer) entity, ruis, parent instanceof UpdateContainer, metadata, reader);
        }
    }

    protected void applyPrimitiveUpdateItems(Object entity, IPrimitiveUpdateItem[] puis, IEntityMetaData metadata) {
        if (puis == null) {
            return;
        }

        for (var pui : puis) {
            var memberName = pui.getMemberName();
            var newValue = pui.getNewValue();
            var member = metadata.getMemberByName(memberName);
            member.setValue(entity, newValue);
        }
    }

    protected void applyRelationUpdateItems(IObjRefContainer entity, IRelationUpdateItem[] ruis, boolean isUpdate, IEntityMetaData metadata, IReader reader) {
        var toPrefetch = new ArrayList<>();
        var relationMembers = metadata.getRelationMembers();
        for (var rui : ruis) {
            var memberName = rui.getMemberName();
            var relationIndex = metadata.getIndexByRelationName(memberName);
            if (ValueHolderState.INIT == entity.get__State(relationIndex)) {
                throw new IllegalStateException("ValueHolder already initialized for property '" + memberName + "'");
            }

            var existingORIs = entity.get__ObjRefs(relationIndex);
            var addedORIs = rui.getAddedORIs();
            var removedORIs = rui.getRemovedORIs();

            IObjRef[] newORIs;
            if (existingORIs.length == 0) {
                if (removedORIs != null && addedORIs.length > 0) {
                    throw new IllegalArgumentException("Removing from empty member");
                }
                newORIs = addedORIs != null ? addedORIs : IObjRef.EMPTY_ARRAY;
            } else {
                // Set to efficiently remove entries
                var existingORIsSet = new LinkedHashSet<>(existingORIs);
                if (removedORIs != null && removedORIs.length > 0) {
                    for (var removedORI : removedORIs) {
                        if (!existingORIsSet.remove(removedORI)) {
                            throw OptimisticLockUtil.throwModified(objRefHelper.entityToObjRef(entity), null, entity);
                        }
                    }
                }
                if (addedORIs != null && addedORIs.length > 0) {
                    for (var addedORI : addedORIs) {
                        if (!existingORIsSet.add(addedORI)) {
                            throw OptimisticLockUtil.throwModified(objRefHelper.entityToObjRef(entity), null, entity);
                        }
                    }
                }
                if (existingORIsSet.isEmpty()) {
                    newORIs = IObjRef.EMPTY_ARRAY;
                } else {
                    newORIs = existingORIsSet.toArray(IObjRef.class);
                }
            }

            var member = relationMembers[relationIndex];
            if (isUpdate) {
                entity.set__ObjRefs(relationIndex, newORIs);
                if (!entity.is__Initialized(relationIndex)) {
                    var dvhr = new DirectValueHolderRef(entity, member);
                    toPrefetch.add(dvhr);
                }
            } else {
                resolveAndSetEntities(entity, newORIs, member, reader);
            }
        }
        if (!toPrefetch.isEmpty()) {
            var objectFuture = new PrefetchFuture(toPrefetch);
            var command = commandBuilder.build(reader.getCommandTypeRegistry(), objectFuture, null);
            reader.addObjectCommand(command);
        }
    }

    protected void resolveAndSetEntities(Object entity, IObjRef[] newORIs, RelationMember member, IReader reader) {
        if (!member.isToMany()) {
            if (newORIs.length == 0) {
                return;
            } else if (newORIs.length == 1) {
                var objectFuture = new ObjRefFuture(newORIs[0]);
                var command = commandBuilder.build(reader.getCommandTypeRegistry(), objectFuture, entity, member);
                reader.addObjectCommand(command);
            } else {
                throw new IllegalArgumentException("Multiple values for to-one relation");
            }
        } else {
            var coll = ListUtil.createCollectionOfType(member.getRealType(), newORIs.length);

            var useObjectFuture = false;
            var commandBuilder = this.commandBuilder;
            var commandTypeRegistry = reader.getCommandTypeRegistry();
            for (var ori : newORIs) {
                if (!(ori instanceof IDirectObjRef)) {
                    var objectFuture = new ObjRefFuture(ori);
                    ;
                    var command = commandBuilder.build(commandTypeRegistry, objectFuture, coll);
                    reader.addObjectCommand(command);
                    useObjectFuture = true;
                    continue;
                }

                var item = ((IDirectObjRef) ori).getDirect();
                if (useObjectFuture) {
                    var command = commandBuilder.build(commandTypeRegistry, null, coll, item);
                    reader.addObjectCommand(command);
                } else {
                    coll.add(item);
                }
            }
        }
    }
}
