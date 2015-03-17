package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.mixin.EntityEqualsMixin;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.util.IPrintable;

public class EntityEqualsVisitor extends ClassGenerator
{
	public static final Class<?> templateType = EntityEqualsMixin.class;

	public static final String templatePropertyName = "__" + templateType.getSimpleName();

	private static final MethodInstance entityEquals_Equals = new MethodInstance(null, templateType, boolean.class, "equals", IEntityEquals.class, Object.class);

	private static final MethodInstance entityEquals_HashCode = new MethodInstance(null, templateType, int.class, "hashCode", IEntityEquals.class);

	private static final MethodInstance entityEquals_toString_Obj = new MethodInstance(null, templateType, String.class, "toString", IEntityEquals.class,
			IPrintable.class);

	private static final MethodInstance entityEquals_toString_Printable = new MethodInstance(null, templateType, void.class, "toString", IEntityEquals.class,
			StringBuilder.class);

	public static PropertyInstance getEntityEqualsTemplateProperty(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance p_embeddedTypeTemplate = PropertyInstance.findByTemplate(templatePropertyName, bean.getClass(), true);
		if (p_embeddedTypeTemplate != null)
		{
			return p_embeddedTypeTemplate;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	public EntityEqualsVisitor(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		implementIEntityEqualsCode();
		super.visitEnd();
	}

	protected void implementIEntityEqualsCode()
	{
		PropertyInstance p_entityEqualsTemplate = getEntityEqualsTemplateProperty(this);
		implementEqualsMethod(p_entityEqualsTemplate);
		implementHashCodeMethod(p_entityEqualsTemplate);
		implementToStringMethod(p_entityEqualsTemplate);
	}

	protected void implementEqualsMethod(PropertyInstance p_entityEqualsTemplate)
	{
		MethodInstance methodTemplate = new MethodInstance(null, Object.class, boolean.class, "equals", Object.class);
		MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
		if (Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
		{
			MethodGenerator mg = visitMethod(methodTemplate);
			mg.callThisGetter(p_entityEqualsTemplate);
			mg.loadThis();
			mg.loadArgs();
			mg.invokeVirtual(entityEquals_Equals);
			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementHashCodeMethod(PropertyInstance p_entityEqualsTemplate)
	{
		MethodInstance methodTemplate = new MethodInstance(null, Object.class, int.class, "hashCode");
		MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
		if (Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
		{
			MethodGenerator mg = visitMethod(methodTemplate);
			mg.callThisGetter(p_entityEqualsTemplate);
			mg.loadThis();
			mg.loadArgs();
			mg.invokeVirtual(entityEquals_HashCode);

			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementToStringMethod(PropertyInstance p_entityEqualsTemplate)
	{
		{
			MethodInstance methodTemplate = new MethodInstance(null, Object.class, String.class, "toString");
			MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
			if (method == null || Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
			{
				MethodGenerator mg = visitMethod(methodTemplate);
				mg.callThisGetter(p_entityEqualsTemplate);
				mg.loadThis();
				mg.loadThis();
				mg.invokeVirtual(entityEquals_toString_Obj);
				mg.returnValue();
				mg.endMethod();
			}
		}

		{
			MethodInstance methodTemplate = new MethodInstance(null, IPrintable.class, void.class, "toString", StringBuilder.class);
			MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
			if (method == null || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
			{
				MethodGenerator mg = visitMethod(methodTemplate);
				mg.callThisGetter(p_entityEqualsTemplate);
				mg.loadThis();
				mg.loadArgs();
				mg.invokeVirtual(entityEquals_toString_Printable);
				mg.returnValue();
				mg.endMethod();
			}
		}
	}
}