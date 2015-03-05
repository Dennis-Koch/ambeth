package de.osthus.esmeralda.handler;

import java.io.StringWriter;
import java.util.List;
import java.util.regex.Pattern;

import javax.lang.model.element.VariableElement;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.misc.EsmeraldaWriter;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.NoOpWriter;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.ClassFile;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ASTHelper implements IASTHelper
{
	public static final Pattern genericTypeExtendsPattern = Pattern.compile("\\? extends ");

	public static final Pattern questionmarkPattern = Pattern.compile("\\?");

	public static final HashSet<String> primitiveTypeSet = new HashSet<String>();

	public static final HashSet<String> boxedPrimitiveTypeSet = new HashSet<String>();

	public static final HashMap<String, String> unboxedToBoxedTypeMap = new HashMap<String, String>();

	public static final HashMap<String, String> boxedToUnboxedTypeMap = new HashMap<String, String>();

	public static final HashSet<String> numberTypeSet = new HashSet<String>();

	static
	{
		addBoxMapping(Boolean.TYPE.getName(), Boolean.class.getName());
		addBoxMapping(Byte.TYPE.getName(), Byte.class.getName());
		addBoxMapping(Character.TYPE.getName(), Character.class.getName());
		addBoxMapping(Short.TYPE.getName(), Short.class.getName());
		addBoxMapping(Integer.TYPE.getName(), Integer.class.getName());
		addBoxMapping(Float.TYPE.getName(), Float.class.getName());
		addBoxMapping(Long.TYPE.getName(), Long.class.getName());
		addBoxMapping(Double.TYPE.getName(), Double.class.getName());

		numberTypeSet.addAll(new String[] { byte.class.getName(), Byte.class.getName(), short.class.getName(), Short.class.getName(), int.class.getName(),
				Integer.class.getName(), long.class.getName(), Long.class.getName(), float.class.getName(), Float.class.getName(), double.class.getName(),
				Double.class.getName() });
	}

	protected static void addBoxMapping(String unboxedType, String boxedType)
	{
		primitiveTypeSet.add(unboxedType);
		boxedPrimitiveTypeSet.add(boxedType);
		unboxedToBoxedTypeMap.put(unboxedType, boxedType);
		boxedToUnboxedTypeMap.put(boxedType, unboxedType);
	}

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
	public String[] parseGenericType(String fqTypeName)
	{
		int genericBracketCounter = 0;
		int firstBracketOpening = -1;
		int lastBracketOpening = -1;
		int lastBracketClosing = -1;
		for (int a = 0, size = fqTypeName.length(); a < size; a++)
		{
			char oneChar = fqTypeName.charAt(a);
			if (oneChar == '<')
			{
				if (genericBracketCounter == 0)
				{
					lastBracketOpening = a;
					lastBracketClosing = -1;
				}
				if (firstBracketOpening == -1)
				{
					firstBracketOpening = a;
				}
				genericBracketCounter++;
				continue;
			}
			else if (oneChar == '>')
			{
				genericBracketCounter--;
				if (genericBracketCounter == 0)
				{
					lastBracketClosing = a;
				}
				continue;
			}
			else if (oneChar == '.')
			{
				if (genericBracketCounter == 0)
				{
					// reset the bracket index
					lastBracketOpening = -1;
					lastBracketClosing = -1;
					continue;
				}
			}
		}
		if (genericBracketCounter != 0)
		{
			throw new IllegalArgumentException(fqTypeName);
		}
		if (lastBracketOpening == -1)
		{
			return new String[] { fqTypeName };
		}
		String nonGenericType = fqTypeName.substring(0, firstBracketOpening) + fqTypeName.substring(lastBracketClosing + 1);
		String genericTypeArguments = fqTypeName.substring(lastBracketOpening + 1, lastBracketClosing);
		return new String[] { nonGenericType, genericTypeArguments };
	}

	@Override
	public String extractNonGenericType(String typeName)
	{
		String[] parts = typeName.split("<", 2);
		return parts[0];
	}

	@Override
	public boolean isNumber(String typeName)
	{
		return numberTypeSet.contains(typeName);
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
			if (parseGenericType(typeName)[0].equals(typeName))
			{
				// trim the default generic that may exist
				return extractNonGenericType(resolvedClassInfo.getFqName());
			}
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
		return typeName;
	}

	@Override
	public String getTypeNameForLiteralKind(Kind kind)
	{
		switch (kind)
		{
			case BOOLEAN_LITERAL:
			{
				return boolean.class.getName();
			}
			case CHAR_LITERAL:
			{
				return char.class.getName();
			}
			case FLOAT_LITERAL:
			{
				return float.class.getName();
			}
			case DOUBLE_LITERAL:
			{
				return double.class.getName();
			}
			case INT_LITERAL:
			{
				return int.class.getName();
			}
			case LONG_LITERAL:
			{
				return long.class.getName();
			}
			case STRING_LITERAL:
			{
				return String.class.getName();
			}
			case NULL_LITERAL:
			{
				return null;
			}
			default:
			{
				throw new RuntimeException("Kind not supported: " + kind);
			}
		}
	}

	@Override
	public String[] splitTypeArgument(String typeArguments)
	{
		ArrayList<String> splittedTypeArguments = new ArrayList<String>();
		int genericLevelCount = 0;
		StringBuilder collectedTypeArgument = new StringBuilder();
		for (int a = 0, size = typeArguments.length(); a < size; a++)
		{
			char oneChar = typeArguments.charAt(a);
			if (genericLevelCount > 0)
			{
				collectedTypeArgument.append(oneChar);
				if (oneChar == '>')
				{
					genericLevelCount--;
				}
				else if (oneChar == '<')
				{
					genericLevelCount++;
				}
				continue;
			}
			if (genericLevelCount == 0 && oneChar == ',')
			{
				splittedTypeArguments.add(collectedTypeArgument.toString().trim());
				collectedTypeArgument.setLength(0);
				continue;
			}
			collectedTypeArgument.append(oneChar);
			if (oneChar == '<')
			{
				genericLevelCount++;
			}
		}
		if (collectedTypeArgument.length() > 0)
		{
			splittedTypeArguments.add(collectedTypeArgument.toString().trim());
			collectedTypeArgument.setLength(0);
		}
		return splittedTypeArguments.toArray(String.class);
	}

	@Override
	public String writeToStash(IBackgroundWorkerDelegate run)
	{
		IConversionContext context = this.context.getCurrent();
		StringWriter stringWriter = new StringWriter();
		IWriter oldWriter = context.getWriter();
		context.startWriteToStash();
		context.setWriter(new EsmeraldaWriter(stringWriter));
		try
		{
			run.invoke();
			return stringWriter.toString();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			context.setWriter(oldWriter);
			context.endWriteToStash();
		}
	}

	@Override
	public <R> R writeToStash(IResultingBackgroundWorkerDelegate<R> run)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter oldWriter = context.getWriter();
		context.startWriteToStash();
		context.setWriter(new EsmeraldaWriter(new NoOpWriter()));
		try
		{
			return run.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			context.setWriter(oldWriter);
			context.endWriteToStash();
		}
	}

	@Override
	public <R, A> R writeToStash(IResultingBackgroundWorkerParamDelegate<R, A> run, A arg)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter oldWriter = context.getWriter();
		context.startWriteToStash();
		context.setWriter(new EsmeraldaWriter(new NoOpWriter()));
		try
		{
			return run.invoke(arg);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			context.setWriter(oldWriter);
			context.endWriteToStash();
		}
	}
}
