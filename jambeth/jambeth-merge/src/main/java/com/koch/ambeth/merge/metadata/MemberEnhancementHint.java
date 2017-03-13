package com.koch.ambeth.merge.metadata;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.objectweb.asm.Type;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;
import com.koch.ambeth.ioc.bytecode.ITargetNameEnhancementHint;
import com.koch.ambeth.service.metadata.Member;

public class MemberEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint, Serializable
{
	private static final long serialVersionUID = -4297854443506118537L;

	protected final Class<?> declaringType;

	protected final String memberName;

	public MemberEnhancementHint(Class<?> declaringType, String memberName)
	{
		this.declaringType = declaringType;
		this.memberName = memberName;
	}

	public Class<?> getDeclaringType()
	{
		return declaringType;
	}

	public String getMemberName()
	{
		return memberName;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!getClass().equals(obj.getClass()))
		{
			return false;
		}
		MemberEnhancementHint other = (MemberEnhancementHint) obj;
		return getDeclaringType().equals(other.getDeclaringType()) && getMemberName().equals(other.getMemberName());
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getDeclaringType().hashCode() ^ getMemberName().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType)
	{
		if (MemberEnhancementHint.class.equals(includedHintType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(declaringType) + "$" + Member.class.getSimpleName() + "$" + memberName.replaceAll(Pattern.quote("."), "_");
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": Path=" + declaringType.getSimpleName() + "." + memberName;
	}
}
