package de.osthus.ambeth.bytecode.behavior;

public interface IBytecodeBehaviorExtendable
{
	void registerBytecodeBehavior(IBytecodeBehavior bytecodeBehavior);

	void registerBytecodeBehavior(IBytecodeBehavior bytecodeBehavior, int order);

	void unregisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior);

	void unregisterBytecodeBehavior(IBytecodeBehavior bytecodeBehavior, int order);
}