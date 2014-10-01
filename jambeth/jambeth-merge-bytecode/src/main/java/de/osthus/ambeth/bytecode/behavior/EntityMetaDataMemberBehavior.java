package de.osthus.ambeth.bytecode.behavior;

import java.util.List;

import de.osthus.ambeth.bytecode.EmbeddedEnhancementHint;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataEmbeddedMemberVisitor;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataMemberVisitor;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataPrimitiveMemberVisitor;
import de.osthus.ambeth.bytecode.visitor.EntityMetaDataRelationMemberVisitor;
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
import de.osthus.ambeth.typeinfo.IPropertyInfo;
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
		RelationMemberEnhancementHint relationMemberHint = state.getContext(RelationMemberEnhancementHint.class);

		String[] memberNameSplit = EmbeddedMember.split(memberHint.getMemberName());
		if (memberNameSplit.length == 1)
		{
			IPropertyInfo[] propertyPath = new IPropertyInfo[1];
			propertyPath[0] = propertyInfoProvider.getProperty(memberHint.getDeclaringType(), memberHint.getMemberName());
			visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.getDeclaringType(), memberHint.getDeclaringType(), memberHint.getMemberName(),
					entityMetaDataProvider, propertyPath);
			if (relationMemberHint != null)
			{
				visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.getDeclaringType(), memberHint.getMemberName(), propertyPath);
			}
			else
			{
				visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.getDeclaringType(), memberHint.getMemberName(), propertyPath);
			}
			return visitor;
		}
		Member[] members = new Member[memberNameSplit.length];
		Class<?> currType = memberHint.getDeclaringType();
		IPropertyInfo[] propertyPath = new IPropertyInfo[members.length];
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = memberNameSplit.length; a < size; a++)
		{
			if (a + 1 < memberNameSplit.length)
			{
				members[a] = memberTypeProvider.getMember(currType, memberNameSplit[a]);
			}
			else if (relationMemberHint != null)
			{
				members[a] = memberTypeProvider.getRelationMember(currType, memberNameSplit[a]);
			}
			else
			{
				members[a] = memberTypeProvider.getPrimitiveMember(currType, memberNameSplit[a]);
			}
			if (a > 0)
			{
				sb.append('.');
			}
			sb.append(members[a].getName());
			propertyPath[a] = propertyInfoProvider.getProperty(currType, memberNameSplit[a]);
			if (a + 1 < memberNameSplit.length)
			{
				currType = bytecodeEnhancer.getEnhancedType(members[a].getRealType(),
						new EmbeddedEnhancementHint(memberHint.getDeclaringType(), currType, sb.toString()));
			}
			else
			{
				currType = members[a].getRealType();
			}
		}
		visitor = new EntityMetaDataMemberVisitor(visitor, memberHint.getDeclaringType(), memberHint.getDeclaringType(), memberHint.getMemberName(),
				entityMetaDataProvider, propertyPath);
		if (relationMemberHint != null)
		{
			visitor = new EntityMetaDataRelationMemberVisitor(visitor, memberHint.getDeclaringType(), memberHint.getMemberName(), propertyPath);
		}
		else
		{
			visitor = new EntityMetaDataPrimitiveMemberVisitor(visitor, memberHint.getDeclaringType(), memberHint.getMemberName(), propertyPath);
		}
		visitor = new EntityMetaDataEmbeddedMemberVisitor(visitor, memberHint.getDeclaringType(), memberHint.getMemberName(), members);
		return visitor;
	}
}
