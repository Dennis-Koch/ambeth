package de.osthus.ambeth.bytecode.behavior;

public interface IBytecodeBehaviorExtendable
{
	void registerBytecodeBehavior(IBytecodeBehavior bytecodeBehavior);

	void unregisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior);
}