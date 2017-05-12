using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class RootCacheValueBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                    IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            RootCacheValueEnhancementHint hint = state.GetContext<RootCacheValueEnhancementHint>();
            if (hint == null)
            {
                return visitor;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(hint.EntityType);
            visitor = new RootCacheValueVisitor(visitor, metaData);
            visitor = new EntityMetaDataHolderVisitor(visitor, metaData);
            return visitor;
        }

    }
}
