package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Member;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Label;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.repackaged.org.objectweb.asm.commons.GeneratorAdapter;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public class GetIdMethodCreator extends ClassGenerator
{
	protected final IEntityMetaData metaData;

	private static final MethodInstance template_m_entityEquals_getId = new MethodInstance(null, IEntityEquals.class, Object.class, "get__Id");

	public static MethodInstance getGetId()
	{
		return MethodInstance.findByTemplate(template_m_entityEquals_getId, false);
	}

	public GetIdMethodCreator(ClassVisitor cv, IEntityMetaData metaData)
	{
		super(cv);
		this.metaData = metaData;
	}

	@Override
	public void visitEnd()
	{
		MethodInstance m_get__Id = MethodInstance.findByTemplate(GetIdMethodCreator.template_m_entityEquals_getId, true);
		if (m_get__Id != null)
		{
			super.visitEnd();
			return;
		}
		m_get__Id = new MethodInstance(BytecodeBehaviorState.getState().getNewType(), GetIdMethodCreator.template_m_entityEquals_getId);

		MethodGenerator mg = visitMethod(m_get__Id);

		ITypeInfoItem idMember = metaData.getIdMember();

		Class<?> idType = idMember.getRealType();
		Type typeOfField = Type.getType(idType);
		mg.invokeGetValue(idMember, new Script()
		{
			@Override
			public void execute(MethodGenerator mg)
			{
				mg.loadThis();
			}
		});
		if (idType.isPrimitive())
		{
			int localValue = mg.newLocal(typeOfField);
			mg.storeLocal(localValue);
			mg.loadLocal(localValue);
			Label l_idNotNull = mg.newLabel();
			if (long.class.equals(idType) || double.class.equals(idType) || float.class.equals(idType))
			{
				if (long.class.equals(idType))
				{
					mg.push(0L);
				}
				else if (double.class.equals(idType))
				{
					mg.push(0.0);
				}
				else
				{
					mg.push(0f);
				}
				mg.ifCmp(typeOfField, GeneratorAdapter.NE, l_idNotNull);
			}
			else
			{
				mg.ifZCmp(GeneratorAdapter.NE, l_idNotNull);
			}
			mg.pushNull();
			mg.returnValue();
			mg.mark(l_idNotNull);
			Type boxed = GeneratorAdapter.getBoxedType(typeOfField);
			mg.newInstance(boxed);
			mg.dup();
			mg.loadLocal(localValue);
			mg.visitMethodInsn(Opcodes.INVOKESPECIAL, boxed.getInternalName(), ConstructorInstance.CONSTRUCTOR_NAME, "(" + typeOfField + ")V");
		}
		mg.returnValue();
		mg.endMethod();

		super.visitEnd();
	}

	protected Type getDeclaringType(Member member, Type newEntityType)
	{
		if (member.getDeclaringClass().isInterface())
		{
			return newEntityType;
		}
		return Type.getType(member.getDeclaringClass());
	}
}
