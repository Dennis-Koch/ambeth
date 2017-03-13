package com.koch.ambeth.merge.bytecode.visitor;

import java.lang.reflect.Constructor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.ConstructorInstance;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.service.metadata.IRelationMemberWrite;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.annotation.CascadeLoadMode;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class EntityMetaDataRelationMemberVisitor extends ClassGenerator
{
	protected static final MethodInstance template_m_getCascadeLoadMode = new MethodInstance(null, RelationMember.class, CascadeLoadMode.class,
			"getCascadeLoadMode");

	protected static final MethodInstance template_m_setCascadeLoadMode = new MethodInstance(null, IRelationMemberWrite.class, void.class,
			"setCascadeLoadMode", CascadeLoadMode.class);

	protected static final MethodInstance template_m_isManyTo = new MethodInstance(null, RelationMember.class, boolean.class, "isManyTo");

	protected final Class<?> entityType;

	protected final String memberName;

	protected final IPropertyInfo[] propertyPath;

	public EntityMetaDataRelationMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, IPropertyInfo[] propertyPath)
	{
		super(new InterfaceAdder(cv, IRelationMemberWrite.class));
		this.entityType = entityType;
		this.memberName = memberName;
		this.propertyPath = propertyPath;
	}

	@Override
	public void visitEnd()
	{
		implementCascadeLoadMode();
		implementIsManyTo();
		super.visitEnd();
	}

	protected void implementCascadeLoadMode()
	{
		FieldInstance f_cascadeLoadMode = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "__cascadeLoadMode", null, CascadeLoadMode.class));

		implementGetter(template_m_getCascadeLoadMode, f_cascadeLoadMode);
		implementSetter(template_m_setCascadeLoadMode, f_cascadeLoadMode);

		Constructor<?>[] constructors = BytecodeBehaviorState.getState().getCurrentType().getDeclaredConstructors();
		for (int a = constructors.length; a-- > 0;)
		{
			ConstructorInstance ci = new ConstructorInstance(constructors[a]);
			ci = new ConstructorInstance(Opcodes.ACC_PUBLIC, ci.getSignature(), ci.getParameters());
			MethodGenerator mv = visitMethod(ci);
			mv.loadThis();
			mv.loadArgs();
			mv.invokeSuperOfCurrentMethod();

			mv.putThisField(f_cascadeLoadMode, new Script()
			{
				@Override
				public void execute(MethodGenerator mg)
				{
					mg.pushEnum(CascadeLoadMode.DEFAULT);
				}
			});
			mv.returnValue();
			mv.endMethod();
		}
	}

	protected void implementIsManyTo()
	{
		FieldInstance f_isManyTo = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "__isManyTo", null, boolean.class));

		implementGetter(template_m_isManyTo, f_isManyTo);
	}
}
