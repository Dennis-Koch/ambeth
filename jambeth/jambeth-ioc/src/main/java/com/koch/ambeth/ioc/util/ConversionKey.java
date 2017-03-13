package com.koch.ambeth.ioc.util;

import com.koch.ambeth.util.IPrintable;

public class ConversionKey implements IPrintable
{
	public Class<?> sourceType;

	public Class<?> targetType;

	public ConversionKey()
	{
		// Intended blank
	}

	public ConversionKey(Class<?> sourceType, Class<?> targetType)
	{
		this.sourceType = sourceType;
		this.targetType = targetType;
	}

	@Override
	public int hashCode()
	{
		return (sourceType.hashCode() << 1) ^ targetType.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || !getClass().equals(obj.getClass()))
		{
			return false;
		}
		ConversionKey other = (ConversionKey) obj;
		return sourceType.equals(other.sourceType) && targetType.equals(other.targetType);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClass().getSimpleName()).append(": ").append(sourceType.getSimpleName()).append("->").append(targetType.getSimpleName());
	}
}
