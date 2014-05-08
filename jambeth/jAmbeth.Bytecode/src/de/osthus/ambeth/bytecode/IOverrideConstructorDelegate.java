package de.osthus.ambeth.bytecode;

public interface IOverrideConstructorDelegate
{
	void invoke(ClassGenerator cv, ConstructorInstance superConstructor);
}
