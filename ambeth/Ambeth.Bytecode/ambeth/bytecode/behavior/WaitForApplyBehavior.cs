using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class WaitForApplyBehavior : AbstractBehavior
    {
        public static IBytecodeBehavior Create(IServiceContext beanContext, WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate)
        {
            return beanContext.RegisterWithLifecycle(new WaitForApplyBehavior(waitForApplyBehaviorDelegate)).Finish();
        }

        public static IBytecodeBehavior Create(IServiceContext beanContext, int sleepCount, WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate)
        {
            return beanContext.RegisterWithLifecycle(new WaitForApplyBehavior(sleepCount, waitForApplyBehaviorDelegate)).Finish();
        }

        protected int sleepCount;

        protected readonly WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate;

        public WaitForApplyBehavior(WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate) : this(1, waitForApplyBehaviorDelegate)
        {
            // intended blank
        }

        public WaitForApplyBehavior(int sleepCount, WaitForApplyBehaviorDelegate waitForApplyBehaviorDelegate)
        {
            this.sleepCount = sleepCount;
            this.waitForApplyBehaviorDelegate = waitForApplyBehaviorDelegate;
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors, IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (--sleepCount > 0)
            {
                cascadePendingBehaviors.Add(this);
                return visitor;
            }
            return waitForApplyBehaviorDelegate(visitor, state, remainingPendingBehaviors, cascadePendingBehaviors);
        }
    }
}