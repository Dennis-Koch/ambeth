package com.koch.ambeth.ioc.bytecode;

public interface IEnhancementHint
{
	<T extends IEnhancementHint> T unwrap(Class<T> includedHintType);
}
