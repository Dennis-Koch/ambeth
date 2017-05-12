using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class EntityEqualsBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public override Type[] GetEnhancements()
        {
            return new Type[] { typeof(IEntityEquals) };
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
            if (!IsAnnotationPresent<EntityEqualsAspect>(state.CurrentType))
            {
                return visitor;
            }
            visitor = new InterfaceAdder(visitor, typeof(IEntityEquals), typeof(IPrintable));
            visitor = new GetIdMethodCreator(visitor, metaData);
            visitor = new GetBaseTypeMethodCreator(visitor);
            visitor = new EntityEqualsVisitor(visitor);
            return visitor;
        }
    }
}