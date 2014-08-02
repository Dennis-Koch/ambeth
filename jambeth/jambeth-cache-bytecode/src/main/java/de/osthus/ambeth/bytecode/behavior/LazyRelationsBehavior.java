package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.util.EntityUtil;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataHolderVisitor;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.bytecode.visitor.RelationsGetterVisitor;
import de.osthus.ambeth.bytecode.visitor.SetCacheModificationMethodCreator;
import de.osthus.ambeth.cache.ValueHolderIEC;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.IEmbeddedMember;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;

public class LazyRelationsBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected ValueHolderIEC valueHolderContainerHelper;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IObjRefContainer.class, IValueHolderContainer.class };
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
			for (RelationMember member : metaData.getRelationMembers())
			{
				if (!(member instanceof IEmbeddedMember))
				{
					continue;
				}
				Member cMember = ((IEmbeddedMember) member).getChildMember();
				MethodPropertyInfo prop = (MethodPropertyInfo) propertyInfoProvider.getProperty(cMember.getDeclaringType(), cMember.getName());
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
			for (RelationMember member : metaData.getRelationMembers())
			{
				if (member instanceof IEmbeddedMember)
				{
					continue;
				}
				MethodPropertyInfo prop = (MethodPropertyInfo) propertyInfoProvider.getProperty(member.getDeclaringType(), member.getName());
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
			visitor = new EntityMetaDataHolderVisitor(visitor, metaData);
		}
		visitor = new RelationsGetterVisitor(visitor, metaData, valueHolderContainerHelper, propertyInfoProvider);
		visitor = new SetCacheModificationMethodCreator(visitor);
		return visitor;
	}
}
