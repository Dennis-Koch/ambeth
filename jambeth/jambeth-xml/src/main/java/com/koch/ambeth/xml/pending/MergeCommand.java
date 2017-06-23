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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.OptimisticLockUtil;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.xml.IReader;

public class MergeCommand extends AbstractObjectCommand
		implements IObjectCommand, IInitializingBean {
	@Autowired
	protected ICommandBuilder commandBuilder;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

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
		}
		else if (parent instanceof UpdateContainer) {
			UpdateContainer updateContainer = (UpdateContainer) parent;
			puis = updateContainer.getPrimitives();
			ruis = updateContainer.getRelations();
		}
		else {
			throw new IllegalArgumentException("Unsupported " + IChangeContainer.class.getSimpleName()
					+ " of type '" + parent.getClass().getName() + "'");
		}

		Object entity = objectFuture.getValue();
		IEntityMetaData metadata = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
		applyPrimitiveUpdateItems(entity, puis, metadata);

		if (ruis != null && ruis.length > 0) {
			applyRelationUpdateItems((IObjRefContainer) entity, ruis, parent instanceof UpdateContainer,
					metadata, reader);
		}
	}

	protected void applyPrimitiveUpdateItems(Object entity, IPrimitiveUpdateItem[] puis,
			IEntityMetaData metadata) {
		if (puis == null) {
			return;
		}

		for (IPrimitiveUpdateItem pui : puis) {
			String memberName = pui.getMemberName();
			Object newValue = pui.getNewValue();
			Member member = metadata.getMemberByName(memberName);
			member.setValue(entity, newValue);
		}
	}

	protected void applyRelationUpdateItems(IObjRefContainer entity, IRelationUpdateItem[] ruis,
			boolean isUpdate, IEntityMetaData metadata, IReader reader) {
		IList<Object> toPrefetch = new ArrayList<>();
		RelationMember[] relationMembers = metadata.getRelationMembers();
		for (IRelationUpdateItem rui : ruis) {
			String memberName = rui.getMemberName();
			int relationIndex = metadata.getIndexByRelationName(memberName);
			if (ValueHolderState.INIT == entity.get__State(relationIndex)) {
				throw new IllegalStateException(
						"ValueHolder already initialized for property '" + memberName + "'");
			}

			IObjRef[] existingORIs = entity.get__ObjRefs(relationIndex);
			IObjRef[] addedORIs = rui.getAddedORIs();
			IObjRef[] removedORIs = rui.getRemovedORIs();

			IObjRef[] newORIs;
			if (existingORIs.length == 0) {
				if (removedORIs != null && addedORIs.length > 0) {
					throw new IllegalArgumentException("Removing from empty member");
				}
				newORIs = addedORIs != null ? addedORIs : ObjRef.EMPTY_ARRAY;
			}
			else {
				// Set to efficiently remove entries
				ILinkedSet<IObjRef> existingORIsSet = new LinkedHashSet<>(existingORIs);
				if (removedORIs != null && removedORIs.length > 0) {
					for (IObjRef removedORI : removedORIs) {
						if (!existingORIsSet.remove(removedORI)) {
							throw OptimisticLockUtil.throwModified(oriHelper.entityToObjRef(entity), null,
									entity);
						}
					}
				}
				if (addedORIs != null && addedORIs.length > 0) {
					for (IObjRef addedORI : addedORIs) {
						if (!existingORIsSet.add(addedORI)) {
							throw OptimisticLockUtil.throwModified(oriHelper.entityToObjRef(entity), null,
									entity);
						}
					}
				}
				if (existingORIsSet.isEmpty()) {
					newORIs = ObjRef.EMPTY_ARRAY;
				}
				else {
					newORIs = existingORIsSet.toArray(IObjRef.class);
				}
			}

			RelationMember member = relationMembers[relationIndex];
			if (isUpdate) {
				entity.set__ObjRefs(relationIndex, newORIs);
				if (!entity.is__Initialized(relationIndex)) {
					DirectValueHolderRef dvhr = new DirectValueHolderRef(entity, member);
					toPrefetch.add(dvhr);
				}
			}
			else {
				resolveAndSetEntities(entity, newORIs, member, reader);
			}
		}
		if (!toPrefetch.isEmpty()) {
			IObjectFuture objectFuture = new PrefetchFuture(toPrefetch);
			IObjectCommand command =
					commandBuilder.build(reader.getCommandTypeRegistry(), objectFuture, null);
			reader.addObjectCommand(command);
		}
	}

	protected void resolveAndSetEntities(Object entity, IObjRef[] newORIs, RelationMember member,
			IReader reader) {
		if (!member.isToMany()) {
			if (newORIs.length == 0) {
				return;
			}
			else if (newORIs.length == 1) {
				IObjectFuture objectFuture = new ObjRefFuture(newORIs[0]);
				IObjectCommand command =
						commandBuilder.build(reader.getCommandTypeRegistry(), objectFuture, entity, member);
				reader.addObjectCommand(command);
			}
			else {
				throw new IllegalArgumentException("Multiple values for to-one relation");
			}
		}
		else {
			Collection<Object> coll =
					ListUtil.createCollectionOfType(member.getRealType(), newORIs.length);

			boolean useObjectFuture = false;
			ICommandBuilder commandBuilder = this.commandBuilder;
			ICommandTypeRegistry commandTypeRegistry = reader.getCommandTypeRegistry();
			for (IObjRef ori : newORIs) {
				if (!(ori instanceof IDirectObjRef)) {
					IObjectFuture objectFuture = new ObjRefFuture(ori);;
					IObjectCommand command = commandBuilder.build(commandTypeRegistry, objectFuture, coll);
					reader.addObjectCommand(command);
					useObjectFuture = true;
					continue;
				}

				Object item = ((IDirectObjRef) ori).getDirect();
				if (useObjectFuture) {
					IObjectCommand command = commandBuilder.build(commandTypeRegistry, null, coll, item);
					reader.addObjectCommand(command);
				}
				else {
					coll.add(item);
				}
			}
		}
	}
}
