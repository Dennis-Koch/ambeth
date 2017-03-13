package com.koch.ambeth.merge.metadata;

import org.objectweb.asm.Type;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.service.metadata.RelationMember;

public class RelationMemberEnhancementHint extends MemberEnhancementHint
{
	public RelationMemberEnhancementHint(Class<?> entityType, String memberName)
	{
		super(entityType, memberName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType)
	{
		T hint = super.unwrap(includedHintType);
		if (hint != null)
		{
			return hint;
		}
		if (RelationMemberEnhancementHint.class.equals(includedHintType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(declaringType) + "$" + RelationMember.class.getSimpleName() + "$" + memberName;
	}
}
