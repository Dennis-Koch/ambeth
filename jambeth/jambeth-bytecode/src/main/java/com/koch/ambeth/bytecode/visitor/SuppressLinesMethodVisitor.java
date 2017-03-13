package com.koch.ambeth.bytecode.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SuppressLinesMethodVisitor extends MethodVisitor
{
	public SuppressLinesMethodVisitor(MethodVisitor mv)
	{
		super(Opcodes.ASM4, mv);
	}

	@Override
	public void visitLineNumber(int line, Label start)
	{
		// Do nothing
	}
}
