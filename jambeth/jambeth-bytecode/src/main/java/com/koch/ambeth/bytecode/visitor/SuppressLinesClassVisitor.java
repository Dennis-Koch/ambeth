package com.koch.ambeth.bytecode.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SuppressLinesClassVisitor extends ClassVisitor
{
	public SuppressLinesClassVisitor(ClassVisitor cv)
	{
		super(Opcodes.ASM4, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		return new SuppressLinesMethodVisitor(mv);
	}
}