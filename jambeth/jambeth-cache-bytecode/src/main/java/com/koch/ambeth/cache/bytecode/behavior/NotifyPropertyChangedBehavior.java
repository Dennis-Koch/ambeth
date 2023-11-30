package com.koch.ambeth.cache.bytecode.behavior;

/*-
 * #%L
 * jambeth-cache-bytecode
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.util.EntityUtil;
import com.koch.ambeth.cache.bytecode.visitor.NotifyPropertyChangedClassVisitor;
import com.koch.ambeth.cache.proxy.IPropertyChangeConfigurable;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.merge.propertychange.PropertyChangeEnhancementHint;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.specialized.INotifyCollectionChangedListener;
import com.koch.ambeth.util.model.INotifyPropertyChanged;
import com.koch.ambeth.util.model.INotifyPropertyChangedSource;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import org.objectweb.asm.ClassVisitor;

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * NotifyPropertyChangeBehavior invokes {@link PropertyChangeListener#propertyChange} when a
 * property is changed. The behavior is applied to types that implement
 * {@link INotifyPropertyChanged}
 */
public class NotifyPropertyChangedBehavior extends AbstractBehavior {
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IPropertyInfoProvider propertyInfoProvider;

    @Override
    public Class<?>[] getEnhancements() {
        return new Class<?>[] {
                INotifyCollectionChangedListener.class, INotifyPropertyChanged.class, INotifyPropertyChangedSource.class, PropertyChangeListener.class, IPropertyChangeConfigurable.class
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors, List<IBytecodeBehavior> cascadePendingBehaviors) {
        var expectMetaData = state.getContext(EntityEnhancementHint.class) != null || state.getContext(EmbeddedEnhancementHint.class) != null;
        if (!expectMetaData && state.getContext(PropertyChangeEnhancementHint.class) == null) {
            // ensure LazyRelationsBehavior was invoked
            return visitor;
        }
        // DefaultPropertiesBehavior executes in this cascade
        var metaData = expectMetaData ? entityMetaDataProvider.getMetaData(EntityUtil.getEntityType(state.getContext())) : null;
        var cascadeBehavior = new AbstractBehavior() {
            @Override
            public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors, List<IBytecodeBehavior> cascadePendingBehaviors) {
                // LazyRelationsBehavior executes in this cascade
                var cascadeBehavior = new AbstractBehavior() {
                    @Override
                    public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors, List<IBytecodeBehavior> cascadePendingBehaviors) {
                        // NotifyPropertyChangedBehavior executes in this cascade
                        // add IPropertyChanged

                        visitor = new InterfaceAdder(visitor, INotifyPropertyChanged.class, INotifyPropertyChangedSource.class, PropertyChangeListener.class, INotifyCollectionChangedListener.class,
                                IPropertyChangeConfigurable.class);
                        visitor = beanContext.registerWithLifecycle(new NotifyPropertyChangedClassVisitor(visitor, metaData, null)).finish();
                        return visitor;
                    }
                };
                cascadeBehavior = beanContext.registerWithLifecycle(cascadeBehavior).finish();
                cascadePendingBehaviors.add(cascadeBehavior);
                return visitor;
            }
        };
        cascadeBehavior = beanContext.registerWithLifecycle(cascadeBehavior).finish();
        cascadePendingBehaviors.add(cascadeBehavior);

        // // NotifyPropertyChangedBehavior executes in this cascade
        // Class<?> currentType = state.getCurrentType();
        // if (!IPropertyChanged.class.isAssignableFrom(currentType))
        // {
        // if (!isAnnotationPresent(currentType, PropertyChangeAspect.class) &&
        // !isAnnotationPresent(currentType, DataObjectAspect.class))
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
