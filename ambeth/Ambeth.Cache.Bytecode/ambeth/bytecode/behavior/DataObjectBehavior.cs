using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Collections.Specialized;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class DataObjectBehavior : AbstractBehavior
    {
        public class CascadeBehavior : AbstractBehavior
        {
            private readonly IEntityMetaData metaData;
            
            [Autowired]
            public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

            public CascadeBehavior(IEntityMetaData metaData)
            {
                this.metaData = metaData;
            }

            public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                    IList<IBytecodeBehavior> cascadePendingBehaviors)
            {
                visitor = new InterfaceAdder(visitor, typeof(IDataObject), typeof(INotifyCollectionChangedListener));
                visitor = new DataObjectVisitor(visitor, metaData, PropertyInfoProvider);
                visitor = new SetCacheModificationMethodCreator(visitor);
                
                // ToBeUpdated & ToBeDeleted have to fire PropertyChange-Events by themselves
                String[] properties = { DataObjectVisitor.template_p_toBeUpdated.Name, DataObjectVisitor.template_p_toBeDeleted.Name };

                CascadeBehavior2 cascadeBehavior2 = BeanContext.RegisterWithLifecycle(new CascadeBehavior2(properties)).Finish();
                cascadePendingBehaviors.Add(cascadeBehavior2);

                return visitor;
            }
        }

        public class CascadeBehavior2 : AbstractBehavior
        {
            private readonly String[] properties;

            public CascadeBehavior2(String[] properties)
            {
                this.properties = properties;
            }

            public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                    IList<IBytecodeBehavior> cascadePendingBehaviors)
            {
                visitor = BeanContext.RegisterWithLifecycle(new NotifyPropertyChangedClassVisitor(visitor, properties)).Finish();
                return visitor;
            }
        }
        
        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        public override Type[] GetEnhancements()
        {
            return new Type[] { typeof(IDataObject) };
        }

        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<EntityEnhancementHint>() == null)
            {
                return visitor;
            }

            bool lastBehaviorStanding = remainingPendingBehaviors.Remove(this);

            Type currentType = state.CurrentType;
            if (!typeof(INotifyPropertyChanged).IsAssignableFrom(currentType))
            {
                if (remainingPendingBehaviors.Count == 0 && lastBehaviorStanding)
                {
                    // The type is not being PropertyChange enhanced.
                    return visitor;
                }
                if (remainingPendingBehaviors.Count == 0 && cascadePendingBehaviors.Count == 0)
                {
                    // Mark "last behavior standing" to avoid infinite loop
                    cascadePendingBehaviors.Add(this);
                }
                cascadePendingBehaviors.Add(this);
                return visitor;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(state.OriginalType);

            visitor = new GetIdMethodCreator(visitor, metaData);
            visitor = new SetBeanContextMethodCreator(visitor);

            CascadeBehavior cascadeBehavior = BeanContext.RegisterWithLifecycle(new CascadeBehavior(metaData)).Finish();
            cascadePendingBehaviors.Add(cascadeBehavior);
            return visitor;
        }
    }
}