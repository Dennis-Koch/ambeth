package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Field;
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
			if (!propertyInfo.isWritable() || !propertyInfo.isReadable())
			{
				continue;
			}
			MethodInstance m_setterTemplate = new MethodInstance(((MethodPropertyInfo) propertyInfo).getSetter());
			MethodInstance m_setter = MethodInstance.findByTemplate(m_setterTemplate, true);

			FieldInstance f_backingField = null;
			if (m_setter == null)
			{
				f_backingField = ensureBackingField(propertyInfo);
				if (f_backingField == null)
				{
					continue;
				}
				// implement setter
				implementSetter(m_setterTemplate, f_backingField);
			}

			MethodInstance m_getterTemplate = new MethodInstance(((MethodPropertyInfo) propertyInfo).getGetter());
			MethodInstance m_getter = MethodInstance.findByTemplate(m_getterTemplate, true);

			if (m_getter == null)
			{
				if (f_backingField == null)
				{
					f_backingField = ensureBackingField(propertyInfo);
				}
				if (f_backingField == null)
				{
					continue;
				}
				// implement getter
				implementGetter(m_getterTemplate, f_backingField);
			}
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