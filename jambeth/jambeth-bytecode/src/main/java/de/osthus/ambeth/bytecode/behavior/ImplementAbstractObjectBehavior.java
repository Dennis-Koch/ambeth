package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactory;
import de.osthus.ambeth.bytecode.abstractobject.ImplementAbstractObjectEnhancementHint;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

/**
 * The ImplementAbstractObjectBehavior creates objects that implement an interface registered for {@link IImplementAbstractObjectFactory}. The generated
 * implementations inherit from {@link IImplementAbstractObjectFactory#getBaseType(Class)} if interface type was registered with an base type.
 */
public class ImplementAbstractObjectBehavior extends AbstractBehavior
{
	@Autowired
	protected IImplementAbstractObjectFactory implementAbstractObjectFactory;

	protected boolean isActive(IEnhancementHint hint, Class<?> originalType)
	{
		return hint != null && implementAbstractObjectFactory.isRegistered(originalType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		final Class<?> keyType = state.getOriginalType();
		if (!isActive(getContext(state.getContext()), keyType))
		{
			// behavior not applied
			return visitor;
		}
		final Class<?>[] interfaceTypes = implementAbstractObjectFactory.getInterfaceTypes(keyType);

		cascadePendingBehaviors.addAll(0, remainingPendingBehaviors);
		remainingPendingBehaviors.clear();

		cascadePendingBehaviors.add(new AbstractBehavior()
		{
			@Override
			public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
					List<IBytecodeBehavior> cascadePendingBehaviors)
			{
				for (Class<?> interfaceType : interfaceTypes)
				{
					// implement interfaceType
					visitor = visitType(visitor, interfaceType, cascadePendingBehaviors);
				}
				cascadePendingBehaviors.add(new AbstractBehavior()
				{
					@Override
					public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
							List<IBytecodeBehavior> cascadePendingBehaviors)
					{
						// implement remaining properties and methods of abstractEntityType
						visitor = visitType(visitor, state.getCurrentType(), cascadePendingBehaviors);

						return visitor;
					}
				});

				return visitor;
			}

		});

		// implements interfaces
		visitor = new InterfaceAdder(visitor, interfaceTypes);

		return visitor;
	}

	protected IEnhancementHint getContext(IEnhancementHint hint)
	{
		return hint.unwrap(ImplementAbstractObjectEnhancementHint.class);
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
	protected ClassVisitor visitType(ClassVisitor visitor, Class<?> type, List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		return visitor;
	}

	@Override
	public Class<?> getTypeToExtendFrom(Class<?> originalType, Class<?> currentType, IEnhancementHint hint)
	{
		if (!isActive(getContext(hint), originalType))
		{
			return super.getTypeToExtendFrom(originalType, currentType, hint);
		}
		return implementAbstractObjectFactory.getBaseType(originalType);
	}
}
