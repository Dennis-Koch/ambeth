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

import com.koch.ambeth.ioc.IServiceContext;

public class WaitForApplyBehavior extends AbstractBehavior {
	public static IBytecodeBehavior create(IServiceContext beanContext,
			WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate) {
		return beanContext.registerWithLifecycle(new WaitForApplyBehavior(waitForApplyBehaviorDelegate))
				.finish();
	}

	public static IBytecodeBehavior create(IServiceContext beanContext, int sleepCount,
			WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate) {
		return beanContext
				.registerWithLifecycle(new WaitForApplyBehavior(sleepCount, waitForApplyBehaviorDelegate))
				.finish();
	}

	protected int sleepCount;

	protected final WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate;

	public WaitForApplyBehavior(WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate) {
		this(1, waitForApplyBehaviorDelegate);
	}

	public WaitForApplyBehavior(int sleepCount,
			WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate) {
		this.sleepCount = sleepCount;
		this.waitForApplyBehaviorDelegate = waitForApplyBehaviorDelegate;
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
			List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors) {
		if (--sleepCount > 0) {
			cascadePendingBehaviors.add(this);
			return visitor;
		}
		return waitForApplyBehaviorDelegate.extend(visitor, state, remainingPendingBehaviors,
				cascadePendingBehaviors);
	}
}
