package com.koch.ambeth.cache.bytecode.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.merge.proxy.IEnhancedType;

public class GetBaseTypeMethodCreator extends ClassGenerator
{
	private static final MethodInstance template_m_getBaseType = new MethodInstance(null, IEnhancedType.class, Class.class, "get__BaseType");

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
