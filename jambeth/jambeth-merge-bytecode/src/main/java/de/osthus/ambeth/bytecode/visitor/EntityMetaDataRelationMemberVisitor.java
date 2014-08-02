package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Constructor;
import java.util.Collection;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.metadata.IRelationMemberWrite;
import de.osthus.ambeth.metadata.MemberTypeProvider;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

public class EntityMetaDataRelationMemberVisitor extends ClassGenerator
{
	protected static final MethodInstance template_m_getCascadeLoadMode = new MethodInstance(null, RelationMember.class, CascadeLoadMode.class,
			"getCascadeLoadMode");

	protected static final MethodInstance template_m_setCascadeLoadMode = new MethodInstance(null, IRelationMemberWrite.class, void.class,
			"setCascadeLoadMode", CascadeLoadMode.class);

	protected static final MethodInstance template_m_isManyTo = new MethodInstance(null, RelationMember.class, boolean.class, "isManyTo");

	protected static final MethodInstance template_m_isToMany = new MethodInstance(null, RelationMember.class, boolean.class, "isToMany");

	protected final Class<?> entityType;

	protected final String memberName;

	protected IPropertyInfoProvider propertyInfoProvider;

	public EntityMetaDataRelationMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, IPropertyInfoProvider propertyInfoProvider)
	{
		super(cv);
		this.entityType = entityType;
		this.memberName = memberName;
		this.propertyInfoProvider = propertyInfoProvider;
	}

	@Override
	public void visitEnd()
	{
		IPropertyInfo[] propertyPath = MemberTypeProvider.buildPropertyPath(entityType, memberName, propertyInfoProvider);
		implementCascadeLoadMode(propertyPath);
		implementIsManyTo(propertyPath);
		implementIsToMany(propertyPath);
		super.visitEnd();
	}

	protected void implementCascadeLoadMode(IPropertyInfo[] propertyPath)
	{
		FieldInstance f_cascadeLoadMode = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "cascadeLoadMode", null, CascadeLoadMode.class));

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

	protected void implementIsManyTo(IPropertyInfo[] propertyPath)
	{
		FieldInstance f_isManyTo = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "isManyTo", null, boolean.class));

		implementGetter(template_m_isManyTo, f_isManyTo);
	}

	protected void implementIsToMany(IPropertyInfo[] propertyPath)
	{
		MethodGenerator mv = visitMethod(template_m_isToMany);
		mv.push(Collection.class.isAssignableFrom(propertyPath[propertyPath.length - 1].getPropertyType()));
		mv.returnValue();
		mv.endMethod();
	}

}
