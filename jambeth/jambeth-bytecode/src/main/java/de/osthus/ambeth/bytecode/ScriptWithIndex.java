package de.osthus.ambeth.bytecode;

public interface ScriptWithIndex
{
	void execute(MethodGenerator mg, int fieldIndex);
}