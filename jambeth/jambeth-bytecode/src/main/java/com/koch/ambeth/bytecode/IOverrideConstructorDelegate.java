package com.koch.ambeth.bytecode;

public interface IOverrideConstructorDelegate
{
	void invoke(ClassGenerator cv, ConstructorInstance superConstructor);
}
