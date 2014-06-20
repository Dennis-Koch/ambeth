package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class EntityEqualsVisitor extends ClassGenerator
{
	private static final MethodInstance entityEquals_Equals = new MethodInstance(null, EntityEqualsVisitor.class, boolean.class, "entityEquals_equals",
			IEntityEquals.class, Object.class);

	private static final MethodInstance entityEquals_HashCode = new MethodInstance(null, EntityEqualsVisitor.class, int.class, "entityEquals_hashCode",
			IEntityEquals.class);

	private static final MethodInstance entityEquals_toString_Obj = new MethodInstance(null, EntityEqualsVisitor.class, String.class, "entityEquals_toString",
			IEntityEquals.class, IPrintable.class);

	private static final MethodInstance entityEquals_toString_Printable = new MethodInstance(null, EntityEqualsVisitor.class, void.class,
			"entityEquals_toString", IEntityEquals.class, StringBuilder.class);

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
		implementEqualsMethod();
		implementHashCodeMethod();
		implementToStringMethod();
	}

	protected void implementEqualsMethod()
	{
		MethodInstance methodTemplate = new MethodInstance(null, Object.class, boolean.class, "equals", Object.class);
		MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
		if (Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
		{
			MethodGenerator mg = visitMethod(methodTemplate);
			mg.loadThis();
			mg.loadArgs();
			mg.invokeStatic(entityEquals_Equals);
			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementHashCodeMethod()
	{
		MethodInstance methodTemplate = new MethodInstance(null, Object.class, int.class, "hashCode");
		MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
		if (Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
		{
			MethodGenerator mg = visitMethod(methodTemplate);
			mg.loadThis();
			mg.loadArgs();
			mg.invokeStatic(entityEquals_HashCode);

			mg.returnValue();
			mg.endMethod();
		}
	}

	protected void implementToStringMethod()
	{
		{
			MethodInstance methodTemplate = new MethodInstance(null, Object.class, String.class, "toString");
			MethodInstance method = MethodInstance.findByTemplate(methodTemplate, true);
			if (Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0)
			{
				MethodGenerator mg = visitMethod(methodTemplate);
				mg.loadThis();
				mg.loadThis();
				mg.invokeStatic(entityEquals_toString_Obj);
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
				mg.loadThis();
				mg.loadArgs();
				mg.invokeStatic(entityEquals_toString_Printable);
				mg.returnValue();
				mg.endMethod();
			}
		}
	}

	public static boolean entityEquals_equals(IEntityEquals left, Object right)
	{
		if (right == left)
		{
			return true;
		}
		if (!(right instanceof IEntityEquals))
		{
			return false;
		}
		Object id = left.get__Id();
		if (id == null)
		{
			// Null id can never be equal with something other than itself
			return false;
		}
		IEntityEquals other = (IEntityEquals) right;
		return id.equals(other.get__Id()) && left.get__BaseType().equals(other.get__BaseType());
	}

	public static int entityEquals_hashCode(IEntityEquals left)
	{
		Object id = left.get__Id();
		if (id == null)
		{
			return System.identityHashCode(left);
		}
		return left.get__BaseType().hashCode() ^ id.hashCode();
	}

	public static String entityEquals_toString(IEntityEquals left, IPrintable printable)
	{
		StringBuilder sb = new StringBuilder();
		printable.toString(sb);
		return sb.toString();
	}

	public static void entityEquals_toString(IEntityEquals left, StringBuilder sb)
	{
		sb.append(left.get__BaseType().getName()).append('-');
		StringBuilderUtil.appendPrintable(sb, left.get__Id());
	}
}