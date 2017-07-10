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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashSet;

public abstract class AbstractBehaviorGroup implements IBytecodeBehavior, IInitializingBean {
	@Autowired
	protected IServiceContext beanContext;

	protected final ArrayList<IBytecodeBehavior> childBehaviors = new ArrayList<>();

	protected final LinkedHashSet<Class<?>> supportedEnhancements = new LinkedHashSet<>(0.5f);

	@Override
	public void afterPropertiesSet() throws Throwable {
		// Intended blank
	}

	protected void addDefaultChildBehavior(Class<? extends IBytecodeBehavior> behaviorType) {
		IBytecodeBehavior behavior = beanContext.registerBean(behaviorType).finish();
		childBehaviors.add(behavior);
		supportedEnhancements.addAll(behavior.getEnhancements());
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
			List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors) {
		for (int a = 0, size = childBehaviors.size(); a < size; a++) {
			IBytecodeBehavior childBehavior = childBehaviors.get(a);
			visitor = childBehavior.extend(visitor, state, remainingPendingBehaviors,
					cascadePendingBehaviors);
		}
		return visitor;
	}

	@Override
	public Class<?>[] getEnhancements() {
		return supportedEnhancements.toArray(Class.class);
	}
}
