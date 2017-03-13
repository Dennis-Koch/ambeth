package com.koch.ambeth.merge.bytecode.visitor;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.service.merge.model.IEntityMetaData;

public class ObjRefStoreVisitor extends ObjRefVisitor
{
	public ObjRefStoreVisitor(ClassVisitor cv, IEntityMetaData metaData, int idIndex)
	{
		super(cv, metaData, idIndex);
	}

	@Override
	public void visitEnd()
	{
		super.visitEnd();
	}
}
