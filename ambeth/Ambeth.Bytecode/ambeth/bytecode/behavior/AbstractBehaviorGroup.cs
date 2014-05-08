using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public abstract class AbstractBehaviorGroup : IBytecodeBehavior, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public IServiceContext BeanContext { protected get; set; }

        protected readonly List<IBytecodeBehavior> childBehaviors = new List<IBytecodeBehavior>();

        protected readonly LinkedHashSet<Type> supportedEnhancements = new LinkedHashSet<Type>(0.5f);

        public void AfterPropertiesSet()
        {
            // Intended blank
        }

        protected void AddDefaultChildBehavior(Type behaviorType)
        {
            IBytecodeBehavior behavior = BeanContext.RegisterAnonymousBean<IBytecodeBehavior>(behaviorType).Finish();
            childBehaviors.Add(behavior);
            supportedEnhancements.AddAll(behavior.GetEnhancements());
        }

        public IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors, IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            for (int a = 0, size = childBehaviors.Count; a < size; a++)
            {
                IBytecodeBehavior childBehavior = childBehaviors[a];
                visitor = childBehavior.Extend(visitor, state, remainingPendingBehaviors, cascadePendingBehaviors);
            }
            return visitor;
        }

        public abstract Type GetTypeToExtendFrom(Type originalType, Type currentType, IEnhancementHint hint);

        public Type[] GetEnhancements()
        {
            return supportedEnhancements.ToArray();
        }
    }
}