package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.cache.IParentCacheValueHardRef;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;

public class ParentCacheHardRefVisitor extends ClassGenerator
{
	private static final MethodInstance template_m_setParentCacheValueHardRef = new MethodInstance(null, IParentCacheValueHardRef.class,
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

		implementField(f_pcvhr);
		implementSetter(template_m_setParentCacheValueHardRef, f_pcvhr);
	}
}
