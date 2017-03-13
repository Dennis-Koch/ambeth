package com.koch.ambeth.bytecode.behavior;

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.ioc.IServiceContext;

public class WaitForApplyBehavior extends AbstractBehavior
{
	public static IBytecodeBehavior create(IServiceContext beanContext, WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate)
	{
		return beanContext.registerWithLifecycle(new WaitForApplyBehavior(waitForApplyBehaviorDelegate)).finish();
	}

	public static IBytecodeBehavior create(IServiceContext beanContext, int sleepCount, WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate)
	{
		return beanContext.registerWithLifecycle(new WaitForApplyBehavior(sleepCount, waitForApplyBehaviorDelegate)).finish();
	}

	protected int sleepCount;

	protected final WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate;

	public WaitForApplyBehavior(WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate)
	{
		this(1, waitForApplyBehaviorDelegate);
	}

	public WaitForApplyBehavior(int sleepCount, WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate)
	{
		this.sleepCount = sleepCount;
		this.waitForApplyBehaviorDelegate = waitForApplyBehaviorDelegate;
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (--sleepCount > 0)
		{
			cascadePendingBehaviors.add(this);
			return visitor;
		}
		return waitForApplyBehaviorDelegate.extend(visitor, state, remainingPendingBehaviors, cascadePendingBehaviors);
	}
}
