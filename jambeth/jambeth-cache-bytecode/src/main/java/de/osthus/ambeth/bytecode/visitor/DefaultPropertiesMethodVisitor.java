package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.repackaged.org.objectweb.asm.ClassVisitor;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Opcodes;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.MethodPropertyInfo;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.util.StringConversionHelper;

public class DefaultPropertiesMethodVisitor extends ClassGenerator
{
	protected IPropertyInfo[] propertyInfos;

	protected IThreadLocalObjectCollector objectCollector;

	public DefaultPropertiesMethodVisitor(ClassVisitor cv, IPropertyInfo[] propertyInfos, IThreadLocalObjectCollector objectCollector)
	{
		super(cv);
		this.propertyInfos = propertyInfos;
		this.objectCollector = objectCollector;
	}

	@Override
	public void visitEnd()
	{
		for (IPropertyInfo propertyInfo : propertyInfos)
		{
			Method getter = ((MethodPropertyInfo) propertyInfo).getGetter();
			Method setter = ((MethodPropertyInfo) propertyInfo).getSetter();
			if (getter == null)
			{
				// look for abstract definition of the getter
				getter = ReflectUtil.getDeclaredMethod(true, getState().getCurrentType(), "get" + propertyInfo.getName());
			}
			if (setter == null)
			{
				// look for abstract definition of the setter
				setter = ReflectUtil.getDeclaredMethod(true, getState().getCurrentType(), "set" + propertyInfo.getName(), propertyInfo.getPropertyType());
			}
			MethodInstance m_getterTemplate = getter != null ? new MethodInstance(getter) : null;
			MethodInstance m_setterTemplate = setter != null ? new MethodInstance(setter) : null;
			MethodInstance m_getter = MethodInstance.findByTemplate(m_getterTemplate, true);
			MethodInstance m_setter = MethodInstance.findByTemplate(m_setterTemplate, true);

			if (m_getter != null || m_setter != null)
			{
				// at least one of the accessors is explicitly implemented
				continue;
			}
			FieldInstance f_backingField = ensureBackingField(propertyInfo);
			if (f_backingField == null)
			{
				continue;
			}
			if (m_setterTemplate == null)
			{
				m_setterTemplate = new MethodInstance(null, Opcodes.ACC_PUBLIC, "set" + propertyInfo.getName(), null,
						m_setterTemplate != null ? m_setterTemplate.getReturnType() : Type.VOID_TYPE, f_backingField.getType());
			}
			// implement setter
			implementSetter(m_setterTemplate, f_backingField);

			if (m_getterTemplate == null)
			{
				m_getterTemplate = new MethodInstance(null, Opcodes.ACC_PUBLIC, "get" + propertyInfo.getName(), null, f_backingField.getType());
			}
			// implement getter
			implementGetter(m_getterTemplate, f_backingField);
		}
		super.visitEnd();
	}

	protected FieldInstance ensureBackingField(IPropertyInfo propertyInfo)
	{
		Field backingField = propertyInfo.getBackingField();
		FieldInstance f_backingField;
		if (backingField != null)
		{
			return new FieldInstance(backingField);
		}
		else if (propertyInfo.getDeclaringType().isInterface() || (propertyInfo.getDeclaringType().getModifiers() & Modifier.ABSTRACT) != 0)
		{
			String fieldName = StringConversionHelper.lowerCaseFirst(objectCollector, propertyInfo.getName());
			f_backingField = BytecodeBehaviorState.getState().getAlreadyImplementedField(fieldName);

			if (f_backingField == null)
			{
				String fieldSignature = FieldInstance.getSignatureFromReturnType(((MethodPropertyInfo) propertyInfo).getGetter());

				// add field
				f_backingField = new FieldInstance(Opcodes.ACC_PROTECTED, StringConversionHelper.lowerCaseFirst(objectCollector, propertyInfo.getName()),
						fieldSignature, Type.getType(propertyInfo.getPropertyType()));

				implementField(f_backingField);
			}
			return f_backingField;
		}
		return null;
	}
}
