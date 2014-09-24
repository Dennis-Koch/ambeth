package de.osthus.ambeth.bytecode.visitor;

import de.osthus.ambeth.bytecode.ClassGenerator;
import de.osthus.ambeth.bytecode.FieldInstance;
import de.osthus.ambeth.bytecode.MethodInstance;
import de.osthus.ambeth.bytecode.behavior.BytecodeBehaviorState;

public class FieldAndSetterTemplate
{
	private final String fieldName;
	private final java.lang.reflect.Method setterMethod;
	private final int fieldAccess;

	public FieldAndSetterTemplate(int fieldAccess, String fieldName, java.lang.reflect.Method setterMethod)
	{
		this.fieldAccess = fieldAccess;
		this.fieldName = fieldName;
		this.setterMethod = setterMethod;
	}

	public FieldInstance getField(ClassGenerator cg)
	{
		FieldInstance f_beanContext = BytecodeBehaviorState.getState().getAlreadyImplementedField(fieldName);
		if (f_beanContext == null)
		{
			f_beanContext = implementSetter(cg);
		}
		return f_beanContext;
	}

	public MethodInstance getSetter(ClassGenerator cg)
	{
		MethodInstance setter = MethodInstance.findByTemplate(setterMethod, true);
		if (setter == null)
		{
			implementSetter(cg);
		}
		return MethodInstance.findByTemplate(setterMethod, false);
	}

	protected FieldInstance implementSetter(ClassGenerator cg)
	{
		String fieldSignature = FieldInstance.getSignatureFromParameterType(setterMethod, 0);
		FieldInstance f_beanContext = new FieldInstance(fieldAccess, fieldName, fieldSignature, setterMethod.getParameterTypes()[0]);
		f_beanContext = cg.implementField(f_beanContext);
		cg.implementSetter(new MethodInstance(setterMethod), f_beanContext);
		return f_beanContext;
	}
}
