package de.osthus.ambeth.bytecode.visitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodGenerator;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.Script;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.expr.PropertyExpression;
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
		HashMap<String, List<Method>> nameToMethodsMap = new HashMap<String, List<Method>>();
		for (Method method : ReflectUtil.getMethods(getState().getOriginalType()))
		{
			List<Method> methodList = nameToMethodsMap.get(method.getName());
			if (methodList == null)
			{
				methodList = new ArrayList<Method>();
				nameToMethodsMap.put(method.getName(), methodList);
			}
			methodList.add(method);
		}
		for (IPropertyInfo propertyInfo : propertyInfos)
		{
			Method getter = ((MethodPropertyInfo) propertyInfo).getGetter();
			Method setter = ((MethodPropertyInfo) propertyInfo).getSetter();
			if (getter == null)
			{
				// look for abstract definition of the getter
				getter = ReflectUtil.getDeclaredMethod(true, getState().getCurrentType(), propertyInfo.getPropertyType(), "get" + propertyInfo.getName());
			}
			if (setter == null)
			{
				// look for abstract definition of the setter
				setter = ReflectUtil.getDeclaredMethod(true, getState().getCurrentType(), void.class, "set" + propertyInfo.getName(),
						propertyInfo.getPropertyType());
			}
			if (getter != null && getter.isAnnotationPresent(PropertyExpression.class))
			{
				// this member will be handled by another visitor
				continue;
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
			final FieldInstance f_backingField = ensureBackingField(propertyInfo);
			if (f_backingField == null)
			{
				continue;
			}
			if (m_setterTemplate == null)
			{
				m_setterTemplate = new MethodInstance(null, Opcodes.ACC_PUBLIC, m_setterTemplate != null ? m_setterTemplate.getReturnType() : Type.VOID_TYPE,
						"set" + propertyInfo.getName(), null, f_backingField.getType());
			}
			// implement setter
			m_setterTemplate = implementSetter(m_setterTemplate, f_backingField);
			List<Method> allSettersWithSameName = nameToMethodsMap.get(m_setterTemplate.getName());
			if (allSettersWithSameName != null)
			{
				final MethodInstance f_m_setterTemplate = m_setterTemplate;
				for (Method setterWithSameName : allSettersWithSameName)
				{
					MethodInstance m_setterWithSameName = MethodInstance.findByTemplate(setterWithSameName, true);
					if (m_setterWithSameName != null)
					{
						// method is implemented, so nothing to do
						continue;
					}
					MethodGenerator mv = visitMethod(new MethodInstance(setterWithSameName));
					if (mv.getMethod().getParameters().length != 1)
					{
						// this visitor handles only "true" setters with exactly one argument
						continue;
					}
					mv.callThisSetter(m_setterTemplate, new Script()
					{
						@Override
						public void execute(MethodGenerator mg)
						{
							mg.loadArg(0);
							mg.checkCast(f_m_setterTemplate.getParameters()[0]);
						}
					});
					mv.returnVoidOrThis();
					mv.endMethod();
				}
			}
			if (m_getterTemplate == null)
			{
				m_getterTemplate = new MethodInstance(null, Opcodes.ACC_PUBLIC, f_backingField.getType(), "get" + propertyInfo.getName(), null);
			}
			// implement getter
			m_getterTemplate = implementGetter(m_getterTemplate, f_backingField);
			List<Method> allGettersWithSameName = nameToMethodsMap.get(m_getterTemplate.getName());
			if (allGettersWithSameName != null)
			{
				for (Method getterWithSameName : allGettersWithSameName)
				{
					MethodInstance m_getterWithSameName = MethodInstance.findByTemplate(getterWithSameName, true);
					if (m_getterWithSameName != null)
					{
						// method is implemented, so nothing to do
						continue;
					}
					MethodGenerator mv = visitMethod(new MethodInstance(getterWithSameName));
					mv.callThisGetter(m_getterTemplate);
					mv.returnValue();
					mv.endMethod();
				}
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

				f_backingField = implementField(f_backingField);
			}
			return f_backingField;
		}
		return null;
	}
}
