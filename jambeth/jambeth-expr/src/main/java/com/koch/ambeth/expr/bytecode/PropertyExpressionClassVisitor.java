package com.koch.ambeth.expr.bytecode;

import java.lang.reflect.Method;

import org.objectweb.asm.ClassVisitor;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.expr.PropertyExpression;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;

public class PropertyExpressionClassVisitor extends ClassGenerator
{
	public static final Class<?> templateType = PropertyExpressionMixin.class;

	public static final String templatePropertyName = "__" + templateType.getSimpleName();

	private static final MethodInstance mi_propertyExpression_evaluate = new MethodInstance(null, templateType, Object.class, "evaluate",
			IEntityMetaDataHolder.class, String.class, Class.class);

	public static PropertyInstance getPropertyExpressionMixinProperty(ClassGenerator cv)
	{
		Object bean = getState().getBeanContext().getService(templateType);
		PropertyInstance pi = PropertyInstance.findByTemplate(templatePropertyName, bean.getClass(), true);
		if (pi != null)
		{
			return pi;
		}
		return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public PropertyExpressionClassVisitor(ClassVisitor cv)
	{
		super(cv);
	}

	@Override
	public void visitEnd()
	{
		super.visitEnd();

		PropertyInstance p_propertyExpressionMixin = null;

		Method[] methods = getState().getCurrentType().getMethods();

		for (Method method : methods)
		{
			PropertyExpression propertyExpression = method.getAnnotation(PropertyExpression.class);
			if (propertyExpression == null)
			{
				continue;
			}
			if (method.getParameterTypes().length != 0)
			{
				continue;
			}
			MethodInstance m_getterTemplate = new MethodInstance(method);
			MethodInstance m_getter = MethodInstance.findByTemplate(m_getterTemplate, true);
			if (m_getter != null)
			{
				// already implemented
				continue;
			}
			if (p_propertyExpressionMixin == null)
			{
				p_propertyExpressionMixin = getPropertyExpressionMixinProperty(this);
			}
			MethodGenerator mg = visitMethod(m_getterTemplate);
			mg.callThisGetter(p_propertyExpressionMixin);
			mg.loadThis();
			mg.push(propertyExpression.value());
			mg.push(mg.getMethod().getReturnType());
			mg.invokeVirtual(mi_propertyExpression_evaluate);
			mg.unbox(mg.getMethod().getReturnType());
			mg.returnValue();
			mg.endMethod();
		}
	}
}
