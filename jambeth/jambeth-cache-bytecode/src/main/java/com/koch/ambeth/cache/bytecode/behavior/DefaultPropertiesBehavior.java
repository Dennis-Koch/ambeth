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

import com.koch.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.visitor.DefaultPropertiesMethodVisitor;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.merge.propertychange.PropertyChangeEnhancementHint;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class DefaultPropertiesBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EntityEnhancementHint.class) == null && state.getContext(EmbeddedEnhancementHint.class) == null
				&& state.getContext(ImplementAbstractObjectEnhancementHint.class) == null && state.getContext(PropertyChangeEnhancementHint.class) == null)
		{
			return visitor;
		}
		cascadePendingBehaviors.addAll(0, remainingPendingBehaviors);
		remainingPendingBehaviors.clear();

		HashMap<String, IPropertyInfo> allProperties = new HashMap<String, IPropertyInfo>();
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(state.getCurrentType());
		for (IPropertyInfo pi : properties)
		{
			allProperties.put(pi.getName(), pi);
		}
		properties = propertyInfoProvider.getProperties(state.getOriginalType());
		for (IPropertyInfo pi : properties)
		{
			// Only add property if it is not already declared by the current type
			allProperties.putIfNotExists(pi.getName(), pi);
		}
		visitor = new DefaultPropertiesMethodVisitor(visitor, allProperties.toArray(IPropertyInfo.class), objectCollector);
		if (state.getOriginalType().isInterface())
		{
			visitor = new InterfaceAdder(visitor, state.getOriginalType());
		}
		return visitor;
	}
}
