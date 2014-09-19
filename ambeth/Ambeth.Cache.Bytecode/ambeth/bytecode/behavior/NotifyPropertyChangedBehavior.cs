using De.Osthus.Ambeth.Bytecode.Util;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Collections.Specialized;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections.Generic;
using System.ComponentModel;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    /**
     * NotifyPropertyChangeBehavior invokes {@link PropertyChangeListener#propertyChanged} when a property is changed. The behavior is applied to types that
     * implement {@link IPropertyChanged}
     */
    public class NotifyPropertyChangedBehavior : AbstractBehavior
    {
        public class CascadeBehavior : AbstractBehavior
        {
            [Property]
            public IEntityMetaData MetaData { protected get; set; }

            public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                        IList<IBytecodeBehavior> cascadePendingBehaviors)
            {
                // LazyRelationsBehavior executes in this cascade
                CascadeBehavior2 cascadeBehavior2 = BeanContext.RegisterAnonymousBean<CascadeBehavior2>().PropertyValue("MetaData", MetaData).Finish();

                cascadePendingBehaviors.Add(cascadeBehavior2);
                return visitor;
            }
        }

        public class CascadeBehavior2 : AbstractBehavior
        {
            [Property]
            public IEntityMetaData MetaData { protected get; set; }

            public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                                    IList<IBytecodeBehavior> cascadePendingBehaviors)
            {
                // NotifyPropertyChangedBehavior executes in this cascade
                // add IPropertyChanged
                visitor = new InterfaceAdder(visitor, typeof(INotifyPropertyChanged), typeof(INotifyPropertyChangedSource),
                    typeof(IPropertyChangedEventHandler), typeof(INotifyCollectionChangedListener), typeof(IPropertyChangeConfigurable));
                visitor = BeanContext.RegisterWithLifecycle(new NotifyPropertyChangedClassVisitor(visitor, MetaData, null)).Finish();
                return visitor;
            }
        }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }
        
        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                            IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<EntityEnhancementHint>() == null && state.GetContext<EmbeddedEnhancementHint>() == null)
            {
                // ensure LazyRelationsBehavior was invoked
                return visitor;
            }
            // DefaultPropertiesBehavior executes in this cascade
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(EntityUtil.GetEntityType(state.Context));
            CascadeBehavior cascadeBehavior = BeanContext.RegisterAnonymousBean<CascadeBehavior>().PropertyValue("MetaData", metaData).Finish();
            cascadePendingBehaviors.Add(cascadeBehavior);

            // // NotifyPropertyChangedBehavior executes in this cascade
            // Type currentType = state.getCurrentType();
            // if (!IPropertyChanged.class.isAssignableFrom(currentType))
            // {
            // if (!isAnnotationPresent(currentType, PropertyChangeAspect.class) && !isAnnotationPresent(currentType, DataObjectAspect.class))
            // {
            // // behavior not applied
            // return visitor;
            // }
            //
            // // add IPropertyChanged
            // visitor = new InterfaceAdder(visitor, Type.getInternalName(IPropertyChanged.class));
            // }
            //
            // IPropertyInfo[] propertyInfos = propertyInfoProvider.getProperties(currentType);
            // visitor = new NotifyPropertyChangedMethodVisitor(visitor, propertyInfos, objectCollector);
            // visitor = new PublicConstructorVisitor(visitor);
            return visitor;
        }
    }
}