using De.Osthus.Ambeth.Bytecode.AbstractObject;
using De.Osthus.Ambeth.Bytecode.Visitor;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public class DefaultPropertiesBehavior : AbstractBehavior
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }
        
        public override IClassVisitor Extend(IClassVisitor visitor, IBytecodeBehaviorState state, IList<IBytecodeBehavior> remainingPendingBehaviors,
                IList<IBytecodeBehavior> cascadePendingBehaviors)
        {
            if (state.GetContext<EntityEnhancementHint>() == null && state.GetContext<EmbeddedEnhancementHint>() == null && state.GetContext<ImplementAbstractObjectEnhancementHint>() == null)
            {
                return visitor;
            }
            ListUtil.AddAll(cascadePendingBehaviors, 0, remainingPendingBehaviors);
            remainingPendingBehaviors.Clear();

            HashMap<String, IPropertyInfo> allProperties = new HashMap<String, IPropertyInfo>();
            IPropertyInfo[] properties = PropertyInfoProvider.GetProperties(state.CurrentType);
            foreach (IPropertyInfo pi in properties)
            {
                allProperties.Put(pi.Name, pi);
            }
            properties = PropertyInfoProvider.GetProperties(state.OriginalType);
            foreach (IPropertyInfo pi in properties)
            {
                // Only add property if it is not already declared by the current type
                allProperties.PutIfNotExists(pi.Name, pi);
            }
            visitor = new DefaultPropertiesMethodVisitor(visitor, allProperties.ToArray());
            if (state.OriginalType.IsInterface)
            {
                visitor = new InterfaceAdder(visitor, state.OriginalType);
            }
            return visitor;
        }
    }
}