package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataMemberVisitor;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataPrimitiveMemberVisitor;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataRelationMemberVisitor;
import de.osthus.ambeth.bytecode.visitor.InterfaceAdder;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.metadata.EmbeddedMember;
import de.osthus.ambeth.metadata.IEmbeddedMember;
import de.osthus.ambeth.metadata.IMemberTypeProvider;
import de.osthus.ambeth.metadata.IPrimitiveMemberWrite;
import de.osthus.ambeth.metadata.IRelationMemberWrite;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.MemberEnhancementHint;
import de.osthus.ambeth.metadata.RelationMemberEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

public class EntityMetaDataMemberBehavior extends AbstractBehavior
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Override
	public Class<?>[] getEnhancements()
	{
		return new Class<?>[] { IRelationMemberWrite.class, IPrimitiveMemberWrite.class, IEmbeddedMember.class };
	}

	@Override
	public ClassVisitor extend(ClassVisitor visitor, IBytecodeBehaviorState state, List<IBytecodeBehavior> remainingPendingBehaviors,
			List<IBytecodeBehavior> cascadePendingBehaviors)
	{
		MemberEnhancementHint memberHint = state.getContext(MemberEnhancementHint.class);
		if (memberHint == null)
		{
			return visitor;
		}
		String[] memberNameSplit = EmbeddedMember.split(memberHint.getMemberName());
		if (memberNameSplit.length == 1)
		{
			RelationMemberEnhancementHint relationMemberHint = state.getContext(RelationMemberEnhancementHint.class);
			visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.getEntityType(), memberNameSplit[0], bytecodeEnhancer, entityMetaDataProvider,
					propertyInfoProvider);
			if (relationMemberHint != null)
			{
				visitor = new InterfaceAdder(visitor, IRelationMemberWrite.class);
				visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.getEntityType(), memberNameSplit[0], propertyInfoProvider);
			}
			else
			{
				visitor = new InterfaceAdder(visitor, IPrimitiveMemberWrite.class);
				visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.getEntityType(), memberNameSplit[0], propertyInfoProvider);
			}
		}
		else
		{
			visitor = new InterfaceAdder(visitor, IEmbeddedMember.class);
			Member[] member = new Member[memberNameSplit.length];
			Class<?> currType = memberHint.getEntityType();
			for (int a = 0, size = memberNameSplit.length; a < size; a++)
			{
				member[a] = memberTypeProvider.getMember(currType, memberNameSplit[a]);
				currType = member[a].getRealType();
			}
		}
		return visitor;
	}
}
