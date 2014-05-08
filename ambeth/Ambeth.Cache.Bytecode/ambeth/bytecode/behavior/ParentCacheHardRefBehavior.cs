using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class ParentCacheHardRefBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public override Type[] GetEnhancements()
        {
            return new Type[] { typeof(IParentCacheValueHardRef) };
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<EntityEnhancementHint>() == null)
            {
                return visitor;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(state.OriginalType, true);
            if (metaData == null)
            {
                return visitor;
            }
            visitor = new InterfaceAdder(visitor, typeof(IParentCacheValueHardRef));
            visitor = new ParentCacheHardRefVisitor(visitor);
            return visitor;
        }
    }
}