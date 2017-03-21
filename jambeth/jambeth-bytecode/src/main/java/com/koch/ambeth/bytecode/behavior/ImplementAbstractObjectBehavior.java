package com.koch.ambeth.bytecode.behavior;

/*-
 * #%L
 * jambeth-bytecode
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

import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactory;
import com.koch.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IEnhancementHint;

/**
 * The ImplementAbstractObjectBehavior creates objects that implement an interface registered for {@link IImplementAbstractObjectFactory}. The generated
 * implementations inherit from {@link IImplementAbstractObjectFactory#getBaseType(Class)} if interface type was registered with an base type.
 */
public class ImplementAbstractObjectBehavior extends AbstractBehavior
{
	@Autowired
	protected IImplementAbstractObjectFactory implementAbstractObjectFactory;

	protected boolean isActive(IEnhancementHint hint, Class<?> originalType)
	{
		return hint != null && implementAbstractObjectFactory.isRegistered(originalType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		final Class<?> keyType = state.getOriginalType();
		if (!isActive(getContext(state.getContext()), keyType))
		{
			// behavior not applied
			return visitor;
		}
		final Class<?>[] interfaceTypes = implementAbstractObjectFactory.getInterfaceTypes(keyType);

		cascadePendingBehaviors.addAll(0, remainingPendingBehaviors);
		remainingPendingBehaviors.clear();

		cascadePendingBehaviors.add(new AbstractBehavior()
		{
			@Override
			public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
					List<IBytecodeBehavior> cascadePendingBehaviors)
			{
				for (Class<?> interfaceType : interfaceTypes)
				{
					// implement interfaceType
					visitor = visitType(visitor, interfaceType, cascadePendingBehaviors);
				}
				cascadePendingBehaviors.add(new AbstractBehavior()
				{
					@Override
					public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
							List<IBytecodeBehavior> cascadePendingBehaviors)
					{
						// implement remaining properties and methods of abstractEntityType
						visitor = visitType(visitor, state.getCurrentType(), cascadePendingBehaviors);

						return visitor;
					}
				});

				return visitor;
			}

		});

		// implements interfaces
		visitor = new InterfaceAdder(visitor, interfaceTypes);

		return visitor;
	}

	protected IEnhancementHint getContext(IEnhancementHint hint)
	{
		return hint.unwrap(ImplementAbstractObjectEnhancementHint.class);
	}

	/**
	 * Adds visitors required to implement this type
	 * 
	 * @param visitor
	 *            the last visitor
	 * @param type
	 *            the Type to be implemented
	 * @return The new visitor
	 */
	protected ClassVisitor visitType(ClassVisitor visitor, Class<?> type, List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		return visitor;
	}

	@Override
	public Class<?> getTypeToExtendFrom(Class<?> originalType, Class<?> currentType, IEnhancementHint hint)
	{
		if (!isActive(getContext(hint), originalType))
		{
			return super.getTypeToExtendFrom(originalType, currentType, hint);
		}
		return implementAbstractObjectFactory.getBaseType(originalType);
	}
}
