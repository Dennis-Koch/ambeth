package com.koch.ambeth.cache.bytecode.behavior;

/*-
 * #%L
 * jambeth-cache-bytecode
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

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.WaitForApplyBehavior;
import com.koch.ambeth.bytecode.behavior.WaitForApplyBehaviorDelegate;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.ValueHolderIEC;
import com.koch.ambeth.cache.bytecode.util.EntityUtil;
import com.koch.ambeth.cache.bytecode.visitor.EntityMetaDataHolderVisitor;
import com.koch.ambeth.cache.bytecode.visitor.RelationsGetterVisitor;
import com.koch.ambeth.cache.bytecode.visitor.SetCacheModificationMethodCreator;
import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.IEmbeddedMember;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class LazyRelationsBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected ValueHolderIEC valueHolderContainerHelper;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IObjRefContainer.class, IValueHolderContainer.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		Class<?> entityType = EntityUtil.getEntityType(state.getContext());
		if (entityType == null)
		{
			return visitor;
		}
		final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		if (metaData == null)
		{
			return visitor;
		}
		final boolean addValueHolderContainer;
		if (EmbeddedEnhancementHint.hasMemberPath(state.getContext()))
		{
			for (RelationMember member : metaData.getRelationMembers())
			{
				if (!(member instanceof IEmbeddedMember))
				{
					continue;
				}
				Member cMember = ((IEmbeddedMember) member).getChildMember();
				MethodPropertyInfo prop = (MethodPropertyInfo) propertyInfoProvider.getProperty(cMember.getDeclaringType(), cMember.getName());
				if (state.hasMethod(new MethodInstance(prop.getGetter())) || state.hasMethod(new MethodInstance(prop.getSetter())))
				{
					// Handle this behavior in the next iteration
					cascadePendingBehaviors.add(this);
					return visitor;
				}
			}
			addValueHolderContainer = false;
		}
		else
		{
			for (RelationMember member : metaData.getRelationMembers())
			{
				if (member instanceof IEmbeddedMember)
				{
					continue;
				}
				MethodPropertyInfo prop = (MethodPropertyInfo) propertyInfoProvider.getProperty(member.getDeclaringType(), member.getName());
				if ((prop.getGetter() != null && state.hasMethod(new MethodInstance(prop.getGetter())))
						|| (prop.getSetter() != null && state.hasMethod(new MethodInstance(prop.getSetter()))))
				{
					// Handle this behavior in the next iteration
					cascadePendingBehaviors.add(this);
					return visitor;
				}
			}
			// Add this interface only for real entities, not for embedded types
			addValueHolderContainer = true;
			visitor = new EntityMetaDataHolderVisitor(visitor, metaData);
		}
		visitor = new SetCacheModificationMethodCreator(visitor);
		cascadePendingBehaviors.add(WaitForApplyBehavior.create(beanContext, new WaitForApplyBehaviorDelegate()
		{
			@Override
			public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
					List<IBytecodeBehavior> cascadePendingBehaviors)
			{
				if (addValueHolderContainer)
				{
					visitor = new InterfaceAdder(visitor, IValueHolderContainer.class);
				}
				visitor = new RelationsGetterVisitor(visitor, metaData, valueHolderContainerHelper, propertyInfoProvider);
				return visitor;
			}
		}));
		return visitor;
	}
}
