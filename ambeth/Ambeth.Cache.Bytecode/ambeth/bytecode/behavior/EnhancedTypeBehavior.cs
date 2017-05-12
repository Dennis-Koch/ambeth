using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class EnhancedTypeBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public override Type[] GetEnhancements()
        {
            return new Type[] { typeof(IEnhancedType), typeof(IEntityMetaDataHolder) };
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                    IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if ((state.GetContext<EntityEnhancementHint>() == null && state.GetContext<EmbeddedEnhancementHint>() == null))
            {
                return visitor;
            }
            if (state.GetContext<EntityEnhancementHint>() != null)
		    {
			    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(state.OriginalType);
			    visitor = new InterfaceAdder(visitor, typeof(IEntityMetaDataHolder));
			    visitor = new EntityMetaDataHolderVisitor(visitor, metaData);
		    }
            visitor = new InterfaceAdder(visitor, typeof(IEnhancedType));
            visitor = new GetBaseTypeMethodCreator(visitor);
            return visitor;
        }
    }
}