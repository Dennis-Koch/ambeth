package de.osthus.ambeth.metadata;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class MemberEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint
{
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
		return Type.getInternalName(declaringType) + "$" + Member.class.getSimpleName() + "$" + memberName;
	}
}
