package com.koch.ambeth.ioc.bytecode;

public interface ITargetNameEnhancementHint extends IEnhancementHint
{
	String getTargetName(Class<?> typeToEnhance);
}
