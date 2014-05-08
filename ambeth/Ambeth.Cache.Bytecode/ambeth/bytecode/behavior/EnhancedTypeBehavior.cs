using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class EnhancedTypeBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public override Type[] GetEnhancements()
        {
            return new Type[] { typeof(IEnhancedType) };
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                    IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if ((state.GetContext<EntityEnhancementHint>() == null && state.GetContext<EmbeddedEnhancementHint>() == null))
            {
                return visitor;
            }
            visitor = new InterfaceAdder(visitor, typeof(IEnhancedType));
            visitor = new GetBaseTypeMethodCreator(visitor);
            return visitor;
        }
    }
}