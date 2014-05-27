package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.DataObjectVisitor;
import de.osthus.ambeth.bytecode.visitor.GetIdMethodCreator;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.bytecode.visitor.NotifyPropertyChangedClassVisitor;
import de.osthus.ambeth.bytecode.visitor.SetCacheModificationMethodCreator;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.specialized.INotifyCollectionChangedListener;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.model.INotifyPropertyChanged;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

public class DataObjectBehavior extends AbstractBehavior
{
	public static class CascadeBehavior extends AbstractBehavior
	{
		protected IEntityMetaData metaData;

		@Autowired
		protected IPropertyInfoProvider propertyInfoProvider;

		public CascadeBehavior(IEntityMetaData metaData)
		{
			this.metaData = metaData;
		}

		@Override
		public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
				List<IBytecodeBehavior> cascadePendingBehaviors)
		{
			Class<?> currentType = BytecodeBehaviorState.getState().getCurrentType();
			HashSet<Class<?>> missingTypes = new HashSet<Class<?>>();
			if (!IDataObject.class.isAssignableFrom(currentType))
			{
				missingTypes.add(IDataObject.class);
			}
			if (!INotifyCollectionChangedListener.class.isAssignableFrom(currentType))
			{
				missingTypes.add(INotifyCollectionChangedListener.class);
			}
			if (missingTypes.size() > 0)
			{
				visitor = new InterfaceAdder(visitor, missingTypes.toArray(Class.class));
			}
			visitor = new DataObjectVisitor(visitor, metaData, propertyInfoProvider);
			visitor = new SetCacheModificationMethodCreator(visitor);

			// ToBeUpdated & ToBeDeleted have to fire PropertyChange-Events by themselves
			String[] properties = new String[] { DataObjectVisitor.template_p_toBeUpdated.getName(), DataObjectVisitor.template_p_toBeDeleted.getName() };

			CascadeBehavior2 cascadeBehavior2 = beanContext.registerWithLifecycle(new CascadeBehavior2(properties)).finish();
			cascadePendingBehaviors.add(cascadeBehavior2);

			return visitor;
		}
	}

	public static class CascadeBehavior2 extends AbstractBehavior
	{
		private final String[] properties;

		public CascadeBehavior2(String[] properties)
		{
			this.properties = properties;
		}

		@Override
		public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
				List<IBytecodeBehavior> cascadePendingBehaviors)
		{
			visitor = beanContext.registerWithLifecycle(new NotifyPropertyChangedClassVisitor(visitor, properties)).finish();
			return visitor;
		}
	}

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IDataObject.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EntityEnhancementHint.class) == null)
		{
			return visitor;
		}

		boolean lastBehaviorStanding = remainingPendingBehaviors.remove(this);

		Class<?> currentType = state.getCurrentType();
		if (!INotifyPropertyChanged.class.isAssignableFrom(currentType))
		{
			if (remainingPendingBehaviors.isEmpty() && lastBehaviorStanding)
			{
				// The type is not being PropertyChange enhanced.
				return visitor;
			}
			if (remainingPendingBehaviors.isEmpty() && cascadePendingBehaviors.isEmpty())
			{
				// Mark "last behavior standing" to avoid infinite loop
				cascadePendingBehaviors.add(this);
			}
			cascadePendingBehaviors.add(this);
			return visitor;
		}
		final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(state.getOriginalType(), true);

		visitor = new GetIdMethodCreator(visitor, metaData);

		CascadeBehavior cascadeBehavior = beanContext.registerWithLifecycle(new CascadeBehavior(metaData)).finish();
		cascadePendingBehaviors.add(cascadeBehavior);
		return visitor;
	}
}
