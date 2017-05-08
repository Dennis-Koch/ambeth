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

import java.util.List;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.behavior.AbstractBehavior;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.cache.bytecode.visitor.DataObjectVisitor;
import com.koch.ambeth.cache.bytecode.visitor.GetIdMethodCreator;
import com.koch.ambeth.cache.bytecode.visitor.NotifyPropertyChangedClassVisitor;
import com.koch.ambeth.cache.bytecode.visitor.SetCacheModificationMethodCreator;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.bytecode.EntityEnhancementHint;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.specialized.INotifyCollectionChangedListener;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.model.INotifyPropertyChanged;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class DataObjectBehavior extends AbstractBehavior {
	public static class CascadeBehavior extends AbstractBehavior {
		protected final IEntityMetaData metaData;

		@Autowired
		protected IPropertyInfoProvider propertyInfoProvider;

		public CascadeBehavior(IEntityMetaData metaData) {
			this.metaData = metaData;
		}

		@Override
		public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
				List<IBytecodeBehavior> remainingPendingBehaviors,
				List<IBytecodeBehavior> cascadePendingBehaviors) {
			Class<?> currentType = BytecodeBehaviorState.getState().getCurrentType();
			HashSet<Class<?>> missingTypes = new HashSet<>();
			if (!IDataObject.class.isAssignableFrom(currentType)) {
				missingTypes.add(IDataObject.class);
			}
			if (!INotifyCollectionChangedListener.class.isAssignableFrom(currentType)) {
				missingTypes.add(INotifyCollectionChangedListener.class);
			}
			if (missingTypes.size() > 0) {
				visitor = new InterfaceAdder(visitor, missingTypes.toArray(Class.class));
			}
			visitor = new DataObjectVisitor(visitor, metaData, propertyInfoProvider);
			visitor = new SetCacheModificationMethodCreator(visitor);

			// ToBeUpdated & ToBeDeleted have to fire PropertyChange-Events by themselves
			String[] properties = new String[] {DataObjectVisitor.template_p_toBeUpdated.getName(),
					DataObjectVisitor.template_p_toBeDeleted.getName()};

			CascadeBehavior2 cascadeBehavior2 =
					beanContext.registerWithLifecycle(new CascadeBehavior2(metaData, properties)).finish();
			cascadePendingBehaviors.add(cascadeBehavior2);

			return visitor;
		}
	}

	public static class CascadeBehavior2 extends AbstractBehavior {
		private final IEntityMetaData metaData;

		private final String[] properties;

		public CascadeBehavior2(IEntityMetaData metaData, String[] properties) {
			this.metaData = metaData;
			this.properties = properties;
		}

		@Override
		public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
				List<IBytecodeBehavior> remainingPendingBehaviors,
				List<IBytecodeBehavior> cascadePendingBehaviors) {
			visitor = beanContext.registerWithLifecycle(
					new NotifyPropertyChangedClassVisitor(visitor, metaData, properties)).finish();
			return visitor;
		}
	}

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public Class<?>[] getEnhancements() {
		return new Class<?>[] {IDataObject.class};
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state,
			List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors) {
		if (state.getContext(EntityEnhancementHint.class) == null) {
			return visitor;
		}

		boolean lastBehaviorStanding = remainingPendingBehaviors.remove(this);

		Class<?> currentType = state.getCurrentType();
		if (!INotifyPropertyChanged.class.isAssignableFrom(currentType)) {
			if (remainingPendingBehaviors.isEmpty() && lastBehaviorStanding) {
				// The type is not being PropertyChange enhanced.
				return visitor;
			}
			if (remainingPendingBehaviors.isEmpty() && cascadePendingBehaviors.isEmpty()) {
				// Mark "last behavior standing" to avoid infinite loop
				cascadePendingBehaviors.add(this);
			}
			cascadePendingBehaviors.add(this);
			return visitor;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(state.getOriginalType());

		visitor = new GetIdMethodCreator(visitor, metaData);

		CascadeBehavior cascadeBehavior =
				beanContext.registerWithLifecycle(new CascadeBehavior(metaData)).finish();
		cascadePendingBehaviors.add(cascadeBehavior);
		return visitor;
	}
}
