package com.koch.ambeth.merge.bytecode.behavior;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.DelegateEnhancementHint;
import com.koch.ambeth.ioc.link.LinkContainer;
import com.koch.ambeth.merge.bytecode.visitor.DelegateVisitor;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.IMap;

public class DelegateBehavior extends AbstractBehavior {
	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
			List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors) {
		DelegateEnhancementHint hint = state.getContext(DelegateEnhancementHint.class);
		if (hint == null) {
			return visitor;
		}
		Class<?> targetType = hint.getType();
		String methodName = hint.getMethodName();
		Class<?> parameterType = hint.getParameterType();

		IMap<Method, Method> delegateMethodMap = LinkContainer.buildDelegateMethodMap(targetType,
				methodName, parameterType);

		if (parameterType.isInterface()) {
			visitor = new InterfaceAdder(visitor, parameterType);
		}
		visitor = new DelegateVisitor(visitor, targetType, delegateMethodMap);
		return visitor;
	}
}
