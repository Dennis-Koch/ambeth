package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Field;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.ConstructorInstance;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.IValueResolveDelegate;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.PropertyInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.compositeid.CompositeIdEnhancementHint;
import de.osthus.ambeth.compositeid.CompositeIdMember;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.template.CompositeIdTemplate;
import de.osthus.ambeth.typeinfo.FieldInfoItemASM;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.ReflectUtil;

public class CompositeIdCreator extends ClassGenerator
{
	public static class CompositeIdValueResolveDelegate implements IValueResolveDelegate
	{
		private final FieldInstance[] fields;

		public CompositeIdValueResolveDelegate(FieldInstance[] fields)
		{
			this.fields = fields;
		}

		@Override
		public Object invoke(String fieldName, Class<?> enhancedType)
		{
			FieldAccess fieldAccess = FieldAccess.get(enhancedType);
			ITypeInfoItem[] members = new ITypeInfoItem[fields.length];
			for (int a = members.length; a-- > 0;)
			{
				Field[] field = ReflectUtil.getDeclaredFieldInHierarchy(enhancedType, fields[a].getName());
				members[a] = new FieldInfoItemASM(field[0], fieldAccess);
			}
			return members;
		}

		@Override
		public Class<?> getValueType()
		{
			return ITypeInfoItem[].class;
		}
	}

	public static final Class<?> templateType = CompositeIdTemplate.class;

	protected static final String templatePropertyName = templateType.getSimpleName();

	public static final MethodInstance m_equalsCompositeId = new MethodInstance(null, templateType, boolean.class, "equalsCompositeId", ITypeInfoItem[].class,
			Object.class, Object.class);

	public static final MethodInstance m_hashCodeCompositeId = new MethodInstance(null, templateType, int.class, "hashCodeCompositeId", ITypeInfoItem[].class,
			Object.class);

	public static final MethodInstance m_toStringCompositeId = new MethodInstance(null, templateType, String.class, "toStringCompositeId",
			ITypeInfoItem[].class, Object.class);

	public static final MethodInstance m_toStringSbCompositeId = new MethodInstance(null, templateType, void.class, "toStringSbCompositeId",
			ITypeInfoItem[].class, Object.class, StringBuilder.class);

	public static PropertyInstance getCompositeIdTemplatePI(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance pi = getState().getProperty(templatePropertyName, bean.getClass());
		if (pi != null)
		{
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	public CompositeIdCreator(ClassVisitor cv)
	{
		super(new InterfaceAdder(cv, Type.getInternalName(IPrintable.class)));
	}

	@Override
	public void visitEnd()
	{
		CompositeIdEnhancementHint context = BytecodeBehaviorState.getState().getContext(CompositeIdEnhancementHint.class);
		Member[] idMembers = context.getIdMembers();

		PropertyInstance p_compositeIdTemplate = getCompositeIdTemplatePI(this);

		Type[] constructorTypes = new Type[idMembers.length];
		final FieldInstance[] fields = new FieldInstance[idMembers.length];
		// order does matter here (to maintain field order for debugging purpose on later objects)
		for (int a = 0, size = idMembers.length; a < size; a++)
		{
			Member member = idMembers[a];
			String fieldName = CompositeIdMember.filterEmbeddedFieldName(member.getName());
			constructorTypes[a] = Type.getType(member.getRealType());
			fields[a] = new FieldInstance(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, fieldName, null, constructorTypes[a]);
			implementField(fields[a]);
		}
		{
			MethodGenerator mg = visitMethod(new ConstructorInstance(Opcodes.ACC_PUBLIC, null, constructorTypes));
			mg.loadThis();
			try
			{
				mg.invokeOnExactOwner(new ConstructorInstance(Object.class.getConstructor()));
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			// order does matter here
			for (int a = 0, size = fields.length; a < size; a++)
			{
				final int index = a;
				mg.putThisField(fields[a], new Script()
				{
					@Override
					public void execute(MethodGenerator mg)
					{
						mg.loadArg(index);
					}
				});
			}
			mg.returnValue();
			mg.endMethod();
		}
		PropertyInstance p_idMembers = implementAssignedReadonlyProperty("IdMembers", new CompositeIdValueResolveDelegate(fields));

		{
			// Implement boolean Object.equals(Object)
			MethodGenerator mg = visitMethod(new MethodInstance(null, Object.class, boolean.class, "equals", Object.class));
			// public boolean CompositeIdTemplate.equalsCompositeId(ITypeInfoItem[] members, Object left, Object right)
			implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_equalsCompositeId);
		}
		{
			// Implement int Object.hashCode()
			MethodGenerator mg = visitMethod(new MethodInstance(null, Object.class, int.class, "hashCode"));
			// public int CompositeIdTemplate.hashCodeCompositeId(ITypeInfoItem[] members, Object compositeId)
			implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_hashCodeCompositeId);
		}
		{
			// Implement String Object.toString()
			MethodGenerator mg = visitMethod(new MethodInstance(null, Object.class, String.class, "toString"));
			// public int CompositeIdTemplate.toStringCompositeId(ITypeInfoItem[] members, Object compositeId)
			implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_toStringCompositeId);
		}
		{
			// Implement void IPrintable.toString(StringBuilder)
			MethodGenerator mg = visitMethod(new MethodInstance(null, IPrintable.class, void.class, "toString", StringBuilder.class));
			// public int CompositeIdTemplate.toStringCompositeId(ITypeInfoItem[] members, Object compositeId)
			implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_toStringSbCompositeId);
		}
		super.visitEnd();
	}

	protected static void implementDefaultDelegatingMethod(MethodGenerator mg, PropertyInstance p_compositeIdTemplate, PropertyInstance p_idMembers,
			MethodInstance delegatedMethod)
	{
		mg.callThisGetter(p_compositeIdTemplate);
		mg.callThisGetter(p_idMembers);
		mg.loadThis();
		mg.loadArgs();
		mg.invokeVirtual(delegatedMethod);
		mg.returnValue();
		mg.endMethod();
	}
}
