package com.koch.ambeth.eclipse.databinding.bytecode;

import java.util.List;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.util.EntityUtil;
import com.koch.ambeth.eclipse.databinding.IListChangeListenerSource;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.bytecode.EmbeddedEnhancementHint;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.merge.propertychange.PropertyChangeEnhancementHint;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class EclipseDatabindingBehavior extends AbstractBehavior {
	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public Class<?>[] getEnhancements() {
		return new Class<?>[] {IListChangeListener.class, IListChangeListenerSource.class};
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
			List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors) {
		boolean expectMetaData = state.getContext(EntityEnhancementHint.class) != null
				|| state.getContext(EmbeddedEnhancementHint.class) != null;
		if (!expectMetaData && state.getContext(PropertyChangeEnhancementHint.class) == null) {
			// ensure LazyRelationsBehavior was invoked
			return visitor;
		}
		// DefaultPropertiesBehavior executes in this cascade
		final IEntityMetaData metaData = expectMetaData
				? entityMetaDataProvider.getMetaData(EntityUtil.getEntityType(state.getContext()))
				: null;
		AbstractBehavior cascadeBehavior = new AbstractBehavior() {
			@Override
			public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
					List<IBytecodeBehavior> remainingPendingBehaviors,
					List<IBytecodeBehavior> cascadePendingBehaviors) {
				// LazyRelationsBehavior executes in this cascade
				AbstractBehavior cascadeBehavior = new AbstractBehavior() {
					@Override
					public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
							List<IBytecodeBehavior> remainingPendingBehaviors,
							List<IBytecodeBehavior> cascadePendingBehaviors) {
						// NotifyPropertyChangedBehavior executes in this cascade
						// add IPropertyChanged

						visitor = new InterfaceAdder(visitor, new String[] {
								"Lorg/eclipse/core/databinding/observable/list/IListChangeListener<Ljava/lang/Object;>;",
								null},
								new Class<?>[] {IListChangeListener.class, IListChangeListenerSource.class});
						visitor = beanContext.registerWithLifecycle(
								new EclipseBindingClassVisitor(visitor, metaData, null)).finish();
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
		return visitor;
	}
}
