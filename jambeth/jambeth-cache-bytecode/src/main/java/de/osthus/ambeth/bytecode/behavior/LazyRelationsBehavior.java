package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.util.EntityUtil;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.bytecode.visitor.RelationsGetterVisitor;
import de.osthus.ambeth.bytecode.visitor.SetCacheModificationMethodCreator;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.EmbeddedRelationInfoItem;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.typeinfo.PropertyInfoItem;

public class LazyRelationsBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected ValueHolderIEC valueHolderContainerHelper;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IValueHolderContainer.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		Class<?> entityType = EntityUtil.getEntityType(state.getContext());
		if (entityType == null)
		{
			return visitor;
		}
		final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		if (metaData == null || metaData.getRelationMembers().length == 0)
		{
			return visitor;
		}
		if (EmbeddedEnhancementHint.hasMemberPath(state.getContext()))
		{
			for (IRelationInfoItem member : metaData.getRelationMembers())
			{
				if (!(member instanceof EmbeddedRelationInfoItem))
				{
					continue;
				}
				IRelationInfoItem cMember = ((EmbeddedRelationInfoItem) member).getChildMember();
				MethodPropertyInfo prop = (MethodPropertyInfo) ((PropertyInfoItem) cMember).getProperty();
				if (state.hasMethod(new MethodInstance(prop.getGetter())) || state.hasMethod(new MethodInstance(prop.getSetter())))
				{
					// Handle this behavior in the next iteration
					cascadePendingBehaviors.add(this);
					return visitor;
				}
			}
		}
		else
		{
			for (IRelationInfoItem member : metaData.getRelationMembers())
			{
				if (!(member instanceof PropertyInfoItem))
				{
					continue;
				}
				MethodPropertyInfo prop = (MethodPropertyInfo) ((PropertyInfoItem) member).getProperty();
				if ((prop.getGetter() != null && state.hasMethod(new MethodInstance(prop.getGetter())))
						|| (prop.getSetter() != null && state.hasMethod(new MethodInstance(prop.getSetter()))))
				{
					// Handle this behavior in the next iteration
					cascadePendingBehaviors.add(this);
					return visitor;
				}
			}
			// Add this interface only for real entities, not for embedded types
			visitor = new InterfaceAdder(visitor, IValueHolderContainer.class);
		}
		visitor = new RelationsGetterVisitor(visitor, metaData, valueHolderContainerHelper);
		visitor = new SetCacheModificationMethodCreator(visitor);
		return visitor;
	}
}
