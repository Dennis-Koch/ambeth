package de.osthus.esmeralda.handler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeResolveException;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.ClassFile;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ASTHelper implements IASTHelper
{
	public static final Pattern genericTypePattern = Pattern.compile("\\.?([^<>]+)<(.+)>");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Override
	public boolean hasGenericTypeArguments(Method method)
	{
		MethodTree methodTree = method.getMethodTree();
		return methodTree.getTypeParameters().size() > 0;
	}

	@Override
	public String extractNonGenericType(String typeName)
	{
		Matcher paramGenericTypeMatcher = genericTypePattern.matcher(typeName);
		if (paramGenericTypeMatcher.matches())
		{
			return paramGenericTypeMatcher.group(1);
		}
		return typeName;
	}

	@Override
	public boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType)
	{
		for (Annotation annotation : model.getAnnotations())
		{
			if (annotationType.getName().equals(annotation.getType()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public List<TypeParameterTree> resolveAllTypeParameters()
	{
		IConversionContext context = this.context.getCurrent();
		Method method = context.getMethod();
		ArrayList<TypeParameterTree> allTypeParameters = new ArrayList<TypeParameterTree>();
		List<? extends TypeParameterTree> classTypeParameters = context.getClassInfo().getClassTree().getTypeParameters();
		if (method != null)
		{
			List<? extends TypeParameterTree> typeParameters = method.getMethodTree().getTypeParameters();
			allTypeParameters.addAll(typeParameters);
		}
		int methodTypeIndex = allTypeParameters.size();
		if (methodTypeIndex == 0)
		{
			allTypeParameters.addAll(classTypeParameters);
		}
		else
		{
			for (TypeParameterTree classTypeParameter : classTypeParameters)
			{
				boolean alreadyDeclaredOnMethod = false;
				for (int a = 0; a < methodTypeIndex; a++)
				{
					TypeParameterTree methodTypeParameter = allTypeParameters.get(a);
					if (classTypeParameter.getName().contentEquals(methodTypeParameter.getName()))
					{
						alreadyDeclaredOnMethod = true;
						break;
					}
				}
				if (!alreadyDeclaredOnMethod)
				{
					allTypeParameters.add(classTypeParameter);
				}
			}
		}
		return allTypeParameters;
	}

	@Override
	public String resolveTypeFromVariableName(String variableName)
	{
		ParamChecker.assertParamNotNullOrEmpty(variableName, "variableName");
		Method method = context.getMethod();
		Field field = context.getField();
		JavaClassInfo owningClass = (JavaClassInfo) (method != null ? method.getOwningClass() : field.getOwningClass());

		try
		{
			if ("this".equals(variableName))
			{
				return owningClass.getPackageName() + "." + owningClass.getName();
			}
			if ("super".equals(variableName))
			{
				return owningClass.getNameOfSuperClass();
			}
			// look for stack variables first
			if (method != null)
			{
				for (VariableElement parameter : method.getParameters())
				{
					if (variableName.equals(parameter.getSimpleName().toString()))
					{
						return parameter.asType().toString();
					}
				}
			}
			// look for declared fields up the whole class hierarchy
			ClassFile currOwningClass = owningClass;
			while (currOwningClass != null)
			{
				for (Field currField : currOwningClass.getFields())
				{
					if (variableName.equals(currField.getName()))
					{
						return currField.getFieldType().toString();
					}
				}
				String nameOfSuperClass = currOwningClass.getNameOfSuperClass();
				if (nameOfSuperClass == null)
				{
					break;
				}
				currOwningClass = context.resolveClassInfo(nameOfSuperClass);
			}
			return resolveFqTypeFromTypeName(variableName);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Could not resolve symbol name '" + variableName + "' on method signature: " + method);
		}
	}

	@Override
	public String resolveFqTypeFromTypeName(String typeName)
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo resolvedClassInfo = context.resolveClassInfo(typeName, true);
		if (resolvedClassInfo != null)
		{
			return resolvedClassInfo.getFqName();
		}
		if (context.isGenericTypeSupported())
		{
			// search if the given typeName is just a generic type. if so return the generic name as fqTypeName
			for (TypeParameterTree typeParameter : resolveAllTypeParameters())
			{
				if (typeParameter.getName().contentEquals(typeName))
				{
					return typeName;
				}
			}
		}
		// for (TypeParameterTree typeParameter : resolveAllTypeParameters())
		// {
		// if (typeParameter.getBounds().size() > 0)
		// {
		// throw new TypeResolveException("Bounds not yet supported: " + typeParameter);
		// }
		// if ()
		// JCTypeParameter tp = (JCTypeParameter) typeParameter;
		// Type upperBound = tp.type.getUpperBound();
		// JCTree tree = tp.getTree();
		// }
		throw new TypeResolveException(typeName);
		// if (log.isWarnEnabled())
		// {
		// loggerHistory.warnOnce(log, this, "Could not resolve type '" + typeName + "' in classInfo '" + context.getClassInfo().getPackageName() + "."
		// + context.getClassInfo().getName() + "'");
		// }
		// return typeName;
	}
}
