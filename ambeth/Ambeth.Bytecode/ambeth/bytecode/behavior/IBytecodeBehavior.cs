using De.Osthus.Ambeth.Bytecode.Visitor;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public interface IBytecodeBehavior
    {
        IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
			    IList<IBytecodeBehavior> cascadePendingBehaviors);

	    Type[] GetEnhancements();

	    Type GetTypeToExtendFrom(Type originalType, Type currentType, IEnhancementHint hint);
    }
}
