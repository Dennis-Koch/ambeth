package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.repackaged.org.objectweb.asm.FieldVisitor;

public interface FScript
{
	void execute(FieldVisitor fv);
}