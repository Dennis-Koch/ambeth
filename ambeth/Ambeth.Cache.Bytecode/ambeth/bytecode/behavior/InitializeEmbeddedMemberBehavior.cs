using De.Osthus.Ambeth.Bytecode.Util;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class InitializeEmbeddedMemberBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors, IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            Type entityType = EntityUtil.GetEntityType(state.Context);
            if (entityType == null)
            {
                return visitor;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);
            String memberPath = EmbeddedEnhancementHint.GetMemberPath(state.Context);
            visitor = new InitializeEmbeddedMemberVisitor(visitor, metaData, memberPath, PropertyInfoProvider);
            return visitor;
        }
    }
}