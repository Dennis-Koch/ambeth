package de.osthus.ambeth.bytecode;

public interface IEnhancementHint
{
	<T extends IEnhancementHint> T unwrap(Class<T> includedHintType);
}
