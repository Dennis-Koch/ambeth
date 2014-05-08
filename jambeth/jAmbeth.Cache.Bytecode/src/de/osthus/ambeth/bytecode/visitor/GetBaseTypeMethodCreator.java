package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import de.osthus.ambeth.proxy.IEnhancedType;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class GetBaseTypeMethodCreator extends ClassGenerator
{
	private static final MethodInstance template_m_getBaseType = new MethodInstance(null, IEnhancedType.class, "get__BaseType");

	public static MethodInstance getGetBaseType()
	{
		return MethodInstance.findByTemplate(template_m_getBaseType, false);
	}

	public GetBaseTypeMethodCreator(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		implementGetBaseType();
		super.visitEnd();
	}

	protected void implementGetBaseType()
	{
		MethodInstance getBaseType = MethodInstance.findByTemplate(template_m_getBaseType, true);
		if (getBaseType != null)
		{
			return;
		}
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		MethodGenerator mg = visitMethod(template_m_getBaseType);
		Type originalType = Type.getType(state.getOriginalType());
		mg.push(originalType);
		mg.returnValue();
		mg.endMethod();
	}
}
