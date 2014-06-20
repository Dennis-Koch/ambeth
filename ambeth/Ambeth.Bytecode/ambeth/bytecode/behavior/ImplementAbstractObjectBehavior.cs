using De.Osthus.Ambeth.Bytecode.AbstractObject;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    /**
     * The ImplementAbstractObjectBehavior creates objects that implement an interface registered for {@link IImplementAbstractObjectFactory}. The generated
     * implementations inherit from {@link IImplementAbstractObjectFactory#getBaseType(Class)} if interface type was registered with an base type.
     */
    public class ImplementAbstractObjectBehavior : AbstractBehavior, IInitializingBean
    {
        public class InnerBehavior : AbstractBehavior
        {
            protected readonly Type[] interfaceTypes;

            protected ImplementAbstractObjectBehavior behavior;

            public InnerBehavior(Type[] interfaceTypes, ImplementAbstractObjectBehavior behavior)
            {
                this.interfaceTypes = interfaceTypes;
                this.behavior = behavior;
            }

            public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                        IList<IBytecodeBehavior> cascadePendingBehaviors)
            {
                foreach (Type interfaceType in interfaceTypes)
                {
                    // implement interfaceType
                    visitor = behavior.VisitType(visitor, interfaceType, cascadePendingBehaviors);
                }
                cascadePendingBehaviors.Add(new InnerBehavior2(behavior));

                return visitor;
            }
        }

        public class InnerBehavior2 : AbstractBehavior
        {
            protected ImplementAbstractObjectBehavior behavior;

            public InnerBehavior2(ImplementAbstractObjectBehavior behavior)
            {
                this.behavior = behavior;
            }

            public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                        IList<IBytecodeBehavior> cascadePendingBehaviors)
            {
                // implement remaining properties and methods of abstractEntityType
                visitor = behavior.VisitType(visitor, state.CurrentType, cascadePendingBehaviors);

                return visitor;
            }
        }

        public IImplementAbstractObjectFactory ImplementAbstractObjectFactory { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ImplementAbstractObjectFactory, "ImplementAbstractObjectFactory");
        }

        protected bool IsActive(IEnhancementHint hint, Type originalType)
        {
            return hint != null && ImplementAbstractObjectFactory.IsRegistered(originalType);
        }

        /**
         * {@inheritDoc}
         */
        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            Type keyType = state.OriginalType;
            if (!IsActive(GetContext(state.Context), keyType))
            {
                // behavior not applied
                return visitor;
            }
            Type[] interfaceTypes = ImplementAbstractObjectFactory.GetInterfaceTypes(keyType);

            ListUtil.AddAll(cascadePendingBehaviors, 0, remainingPendingBehaviors);
            remainingPendingBehaviors.Clear();

            cascadePendingBehaviors.Add(new InnerBehavior(interfaceTypes, this));

            // implements interfaces
            visitor = new InterfaceAdder(visitor, interfaceTypes);

            return visitor;
        }

        protected IEnhancementHint GetContext(IEnhancementHint hint)
        {
            return hint.Unwrap<ImplementAbstractObjectEnhancementHint>();
        }

        /**
         * Adds visitors required to implement this type
         * 
         * @param visitor
         *            the last visitor
         * @param type
         *            the Type to be implemented
         * @return The new visitor
         */
        protected virtual IClassVisitor VisitType(IClassVisitor visitor, Type type, IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            return visitor;
        }

        public override Type GetTypeToExtendFrom(Type originalType, Type currentType, IEnhancementHint hint)
        {
            if (!IsActive(GetContext(hint), originalType))
            {
                return base.GetTypeToExtendFrom(originalType, currentType, hint);
            }
            return ImplementAbstractObjectFactory.GetBaseType(originalType);
        }
    }
}