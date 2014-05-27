package de.osthus.ambeth.bytecode;

public interface ITargetNameEnhancementHint extends IEnhancementHint
{
	String getTargetName(Class<?> typeToEnhance);
}
