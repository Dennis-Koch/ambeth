package de.osthus.ambeth.ioc.bytecode;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.util.EqualsUtil;

public class DelegateEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint
{
	private final Class<?> type;

	private final Class<?> parameterType;

	private String methodName;

	public DelegateEnhancementHint(Class<?> type, String methodName, Class<?> parameterType)
	{
		this.type = type;
		this.methodName = methodName;
		this.parameterType = parameterType;
	}

	public Class<?> getType()
	{
		return type;
	}

	public String getMethodName()
	{
		return methodName;
	}

	public Class<?> getParameterType()
	{
		return parameterType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof DelegateEnhancementHint))
		{
			return false;
		}
		DelegateEnhancementHint other = (DelegateEnhancementHint) obj;
		return EqualsUtil.equals(other.getType(), getType()) && EqualsUtil.equals(other.getMethodName(), getMethodName())
				&& EqualsUtil.equals(other.getParameterType(), getParameterType());
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getType().hashCode() ^ getMethodName().hashCode() ^ getParameterType().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (DelegateEnhancementHint.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return getType().getName() + "$Delegate$" + getMethodName();
	}
}
