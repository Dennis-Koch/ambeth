package com.koch.ambeth.bytecode;

import org.objectweb.asm.ClassVisitor;

public interface IBuildVisitorDelegate
{
	ClassVisitor build(ClassVisitor cv);
}
