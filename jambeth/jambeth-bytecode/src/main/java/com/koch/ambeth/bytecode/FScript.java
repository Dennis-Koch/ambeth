package com.koch.ambeth.bytecode;

import org.objectweb.asm.FieldVisitor;

public interface FScript
{
	void execute(FieldVisitor fv);
}