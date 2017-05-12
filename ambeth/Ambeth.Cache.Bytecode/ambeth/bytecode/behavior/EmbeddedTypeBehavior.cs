using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Model;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class EmbeddedTypeBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        public override Type[] GetEnhancements()
        {
            return new Type[] { typeof(IEmbeddedType) };
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                    IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<EmbeddedEnhancementHint>() == null)
            {
                return visitor;
            }
            visitor = new InterfaceAdder(visitor, typeof(IEmbeddedType));
            visitor = new EmbeddedTypeVisitor(visitor);
            return visitor;
        }
    }
}