using De.Osthus.Ambeth.Bytecode.Visitor;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class PublicConstructorBehavior : AbstractBehavior
    {
        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<EntityEnhancementHint>() == null)
            {
                return visitor;
            }
            visitor = new PublicConstructorVisitor(visitor);
            return visitor;
        }
    }
}