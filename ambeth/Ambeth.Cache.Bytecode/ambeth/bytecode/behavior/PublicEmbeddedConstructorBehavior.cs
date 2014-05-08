using De.Osthus.Ambeth.Bytecode.Visitor;
using System.Collections.Generic;
namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class PublicEmbeddedConstructorBehavior : AbstractBehavior
    {
        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                        IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<EmbeddedEnhancementHint>() == null)
            {
                return visitor;
            }
            visitor = new PublicEmbeddedConstructorVisitor(visitor);
            return visitor;
        }
    }
}
