package de.osthus.ambeth.metadata;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class MemberEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint
{
	protected final Class<?> entityType;

	protected final String memberName;

	public MemberEnhancementHint(Class<?> entityType, String memberName)
	{
		this.entityType = entityType;
		this.memberName = memberName;
	}

	public Class<?> getEntityType()
	{
		return entityType;
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
		return getEntityType().equals(other.getEntityType()) && getMemberName().equals(other.getMemberName());
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getEntityType().hashCode() ^ getMemberName().hashCode();
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
		return Type.getInternalName(entityType) + "$" + Member.class.getSimpleName() + "$" + memberName;
	}
}
