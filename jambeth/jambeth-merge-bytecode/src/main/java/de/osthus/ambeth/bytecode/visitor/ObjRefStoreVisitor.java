package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;

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
