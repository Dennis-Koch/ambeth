using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Objrefstore;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class ObjRefStoreBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }


        public Type[] getEnhancements()
        {
            return new Type[] { typeof(ObjRefStore) };
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors, IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            ObjRefStoreEnhancementHint memberHint = state.GetContext<ObjRefStoreEnhancementHint>();
            if (memberHint == null)
            {
                return visitor;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(memberHint.EntityType);
            visitor = new ObjRefStoreVisitor(visitor, metaData, memberHint.IdIndex);
            return visitor;
        }
    }
}