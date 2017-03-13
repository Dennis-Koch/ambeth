package com.koch.ambeth.cache.bytecode.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.cache.IParentCacheValueHardRef;

public class ParentCacheHardRefVisitor extends ClassGenerator
{
	private static final MethodInstance template_m_setParentCacheValueHardRef = new MethodInstance(null, IParentCacheValueHardRef.class, void.class,
			"setParentCacheValueHardRef", Object.class);

	public ParentCacheHardRefVisitor(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		implementParentCacheValueHardRef();
		super.visitEnd();
	}

	protected void implementParentCacheValueHardRef()
	{
		FieldInstance f_pcvhr = new FieldInstance(Opcodes.ACC_PRIVATE, "$pcvhr", template_m_setParentCacheValueHardRef.getSignatureFromParameterType(0),
				template_m_setParentCacheValueHardRef.getParameters()[0]);

		f_pcvhr = implementField(f_pcvhr);
		implementSetter(template_m_setParentCacheValueHardRef, f_pcvhr);
	}
}
