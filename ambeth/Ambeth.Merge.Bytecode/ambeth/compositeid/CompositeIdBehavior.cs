using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Log;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.CompositeId
{
    public class CompositeIdBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<CompositeIdEnhancementHint>() == null)
            {
                return visitor;
            }
            visitor = new CompositeIdCreator(visitor);
            return visitor;
        }
    }
}
