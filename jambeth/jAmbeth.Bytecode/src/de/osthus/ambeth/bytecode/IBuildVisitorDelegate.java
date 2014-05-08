package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

public interface IBuildVisitorDelegate
{
	ClassVisitor build(ClassVisitor cv);
}
