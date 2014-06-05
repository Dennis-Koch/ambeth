using De.Osthus.Ambeth.Bytecode.Util;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Debug;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class LazyRelationsBehavior : AbstractBehavior
    {
        public class CascadeBehavior : AbstractBehavior
        {
            protected IEntityMetaData metaData;

            protected bool implementValueHolderContainerInterface;

            [Autowired]
            public ValueHolderIEC ValueHolderContainerHelper { protected get; set; }

            public CascadeBehavior(IEntityMetaData metaData, bool implementValueHolderContainerInterface)
            {
                this.metaData = metaData;
                this.implementValueHolderContainerInterface = implementValueHolderContainerInterface;
            }

            public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                    IList<IBytecodeBehavior> cascadePendingBehaviors)
            {
                ListUtil.AddAll(cascadePendingBehaviors, 0, remainingPendingBehaviors);
                remainingPendingBehaviors.Clear();
                
                // Add this interface only for real entities, not for embedded types
                if (implementValueHolderContainerInterface)
                {
                    visitor = new InterfaceAdder(visitor, typeof(IValueHolderContainer));
                }
                visitor = new RelationsGetterVisitor(visitor, metaData, ValueHolderContainerHelper);
                visitor = new SetCacheModificationMethodCreator(visitor);
                return visitor;
            }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public ValueHolderIEC ValueHolderContainerHelper { protected get; set; }

        public override Type[] GetEnhancements()
        {
            return new Type[] { typeof(IValueHolderContainer) };
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                        IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            Type entityType = EntityUtil.GetEntityType(state.Context);
            if (entityType == null)
            {
                return visitor;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType, true);
            if (metaData == null)
            {
                return visitor;
            }
            visitor = new FlattenDebugHierarchyVisitor(visitor, metaData.RelationMembers.Length != 0);
            if (metaData.RelationMembers.Length == 0)
            {
                return visitor;
            }
            if (EmbeddedEnhancementHint.HasMemberPath(state.Context))
            {
                foreach (IRelationInfoItem member in metaData.RelationMembers)
                {
                    if (!(member is EmbeddedRelationInfoItem))
                    {
                        continue;
                    }
                    IRelationInfoItem cMember = ((EmbeddedRelationInfoItem)member).ChildMember;
                    MethodPropertyInfo prop = (MethodPropertyInfo)((PropertyInfoItem)cMember).Property;
                    if ((prop.Getter != null && state.HasMethod(new MethodInstance(prop.Getter))) || (prop.Setter != null && state.HasMethod(new MethodInstance(prop.Setter))))
                    {
                        // Handle this behavior in the next iteration
                        cascadePendingBehaviors.Add(this);
                        return visitor;
                    }
                }
            }
            else
            {
                foreach (IRelationInfoItem member in metaData.RelationMembers)
                {
                    if (!(member is PropertyInfoItem))
                    {
                        continue;
                    }
                    MethodPropertyInfo prop = (MethodPropertyInfo)((PropertyInfoItem)member).Property;
                    if (state.HasMethod(new MethodInstance(prop.Getter)) || state.HasMethod(new MethodInstance(prop.Setter)))
                    {
                        // Handle this behavior in the next iteration
                        cascadePendingBehaviors.Add(this);
                        return visitor;
                    }
                }
                // Add this interface only for real entities, not for embedded types
			    visitor = new InterfaceAdder(visitor, typeof(IValueHolderContainer));
            }
            visitor = new RelationsGetterVisitor(visitor, metaData, ValueHolderContainerHelper);
            visitor = new SetCacheModificationMethodCreator(visitor);
            //cascadePendingBehaviors.Add(BeanContext.RegisterWithLifecycle(new CascadeBehavior(metaData, implementValueHolderContainerInterface)).Finish());
            return visitor;
        }
    }
}