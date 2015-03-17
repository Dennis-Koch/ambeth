package de.osthus.ambeth.bytecode.behavior;

import java.beans.PropertyChangeListener;
import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.EntityEnhancementHint;
import de.osthus.ambeth.bytecode.util.EntityUtil;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.bytecode.visitor.NotifyPropertyChangedClassVisitor;
import de.osthus.ambeth.collections.specialized.INotifyCollectionChangedListener;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.model.INotifyPropertyChanged;
import de.osthus.ambeth.model.INotifyPropertyChangedSource;
import de.osthus.ambeth.proxy.IPropertyChangeConfigurable;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

/**
 * NotifyPropertyChangeBehavior invokes {@link PropertyChangeListener#propertyChanged} when a property is changed. The behavior is applied to types that
 * implement {@link INotifyPropertyChanged}
 */
public class NotifyPropertyChangedBehavior extends AbstractBehavior
{
	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { INotifyCollectionChangedListener.class, INotifyPropertyChanged.class, INotifyPropertyChangedSource.class,
				PropertyChangeListener.class, IPropertyChangeConfigurable.class };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		if (state.getContext(EntityEnhancementHint.class) == null && state.getContext(EmbeddedEnhancementHint.class) == null)
		{
			// ensure LazyRelationsBehavior was invoked
			return visitor;
		}
		// DefaultPropertiesBehavior executes in this cascade
		final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(EntityUtil.getEntityType(state.getContext()));
		AbstractBehavior cascadeBehavior = new AbstractBehavior()
		{
			@Override
			public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
					List<IBytecodeBehavior> cascadePendingBehaviors)
			{
				// LazyRelationsBehavior executes in this cascade
				AbstractBehavior cascadeBehavior = new AbstractBehavior()
				{
					@Override
					public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
							List<IBytecodeBehavior> cascadePendingBehaviors)
					{
						// NotifyPropertyChangedBehavior executes in this cascade
						// add IPropertyChanged

						visitor = new InterfaceAdder(visitor, INotifyPropertyChanged.class, INotifyPropertyChangedSource.class, PropertyChangeListener.class,
								INotifyCollectionChangedListener.class, IPropertyChangeConfigurable.class);
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
