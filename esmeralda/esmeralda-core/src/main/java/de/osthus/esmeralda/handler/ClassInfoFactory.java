package de.osthus.esmeralda.handler;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.regex.Matcher;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.WeakHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.esmeralda.CodeVisitor;
import de.osthus.esmeralda.IClassInfoManager;
import de.osthus.esmeralda.handler.uni.expr.MockVariableElement;
import demo.codeanalyzer.common.model.BaseJavaClassModelInfo;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.MethodInfo;

public class ClassInfoFactory implements IClassInfoFactory
{
	public static final HashMap<String, Class<?>> nativeTypesSet = new HashMap<String, Class<?>>();

	static
	{
		nativeTypesSet.put("void", Void.TYPE);
		nativeTypesSet.put("boolean", Boolean.TYPE);
		nativeTypesSet.put("char", Character.TYPE);
		nativeTypesSet.put("byte", Byte.TYPE);
		nativeTypesSet.put("short", Short.TYPE);
		nativeTypesSet.put("int", Integer.TYPE);
		nativeTypesSet.put("long", Long.TYPE);
		nativeTypesSet.put("float", Float.TYPE);
		nativeTypesSet.put("double", Double.TYPE);
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IClassInfoManager classInfoManager;

	protected final Reference<Class<?>> emptyR = new WeakReference<Class<?>>(null);

	protected final WeakHashMap<String, Reference<Class<?>>> alreadyTriedNames = new WeakHashMap<String, Reference<Class<?>>>();

	@Override
	public JavaClassInfo createClassInfo(String fqName)
	{
		if (fqName.endsWith("[]"))
		{
			String componentName = fqName.substring(0, fqName.length() - 2);
			JavaClassInfo componentCI = classInfoManager.resolveClassInfo(componentName, true);
			if (componentCI == null)
			{
				return null;
			}
			JavaClassInfo ci = new JavaClassInfo(classInfoManager);
			ci.setPackageName(componentCI.getPackageName());
			ci.setName(componentCI.getName() + "[]");
			ci.setComponentType(componentCI);
			ci.setNameOfSuperClass(Object.class.getName());

			FieldInfo lengthField = new FieldInfo();
			lengthField.setOwningClass(ci);
			lengthField.setName("length");
			lengthField.setFieldType("int");
			ci.addField(lengthField);
			return ci;
		}
		JavaClassInfo classInfo = new JavaClassInfo(classInfoManager);

		Class<?> type = loadClass(fqName);
		if (type == null)
		{
			return null;
		}
		TypeVariable<?>[] typeParameters = type.getTypeParameters();
		JavaClassInfo[] typeArguments = null;
		boolean first = true;
		boolean hasNullTypeArgument = true;
		for (int a = 0, size = typeParameters.length; a < size; a++)
		{
			TypeVariable<?> typeParameter = typeParameters[a];
			if (first)
			{
				first = false;
				typeArguments = new JavaClassInfo[typeParameters.length];
			}
			Type boundsType = typeParameter.getBounds()[0];
			if (!(boundsType instanceof Class))
			{
				log.warn("Boundary not yet handled: " + boundsType + " on type " + type.getName());
				hasNullTypeArgument = true;
			}
			else
			{
				typeArguments[a] = classInfoManager.resolveClassInfo(((Class<?>) boundsType).getName());
			}
		}
		classInfo.setName(buildSimpleNameOfType(type));
		classInfo.setNonGenericName(astHelper.extractNonGenericType(classInfo.getName()));

		if (!hasNullTypeArgument)
		{
			classInfo.setTypeArguments(typeArguments);
		}

		if (type.getPackage() != null)
		{
			classInfo.setPackageName(type.getPackage().getName());
		}
		if (type.getSuperclass() != null)
		{
			classInfo.setNameOfSuperClass(type.getSuperclass().getName());
		}
		for (Class<?> interfaceType : type.getInterfaces())
		{
			classInfo.addNameOfInterface(interfaceType.getName());
		}
		// TODO: set correct modifiers & type
		setModifiers(type.getModifiers(), classInfo);

		Field[] declaredFields = ReflectUtil.getDeclaredFields(type);
		for (Field declaredField : declaredFields)
		{
			classInfo.addField(mockField(classInfo, declaredField));
		}
		// TODO: mock fields
		Method[] declaredMethods = ReflectUtil.getDeclaredMethods(type);
		for (Method declaredMethod : declaredMethods)
		{
			classInfo.addMethod(mockMethod(classInfo, declaredMethod));
		}
		return classInfo;
	}

	protected String buildSimpleNameOfType(Class<?> type)
	{
		StringBuilder nameSB = new StringBuilder();
		Class<?> enclosingClass = type.getEnclosingClass();
		while (enclosingClass != null)
		{
			nameSB.insert(0, enclosingClass.getSimpleName());
			nameSB.append('.');
			enclosingClass = enclosingClass.getEnclosingClass();
		}
		nameSB.append(type.getSimpleName());

		TypeVariable<?>[] typeParameters = type.getTypeParameters();
		boolean first = true;
		for (int a = 0, size = typeParameters.length; a < size; a++)
		{
			TypeVariable<?> typeParameter = typeParameters[a];
			if (first)
			{
				first = false;
				nameSB.append('<');
			}
			else
			{
				nameSB.append(',');
			}
			nameSB.append(typeParameter.getName());
		}
		if (!first)
		{
			nameSB.append('>');
		}
		return nameSB.toString();
	}

	protected Class<?> loadClass(String fqTypeName)
	{
		Class<?> nativeType = nativeTypesSet.get(fqTypeName);
		if (nativeType != null)
		{
			return nativeType;
		}
		Reference<Class<?>> typeR = alreadyTriedNames.get(fqTypeName);
		if (typeR == emptyR)
		{
			// already tried without success before
			return null;
		}
		Class<?> type = typeR != null ? typeR.get() : null;
		if (type != null)
		{
			// already tried with success before
			return type;
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			try
			{
				return classLoader.loadClass(fqTypeName);
			}
			catch (ClassNotFoundException e)
			{
				alreadyTriedNames.put(fqTypeName, emptyR);
				return classLoader.loadClass("java.lang." + fqTypeName);
			}
		}
		catch (ClassNotFoundException e)
		{
			alreadyTriedNames.put(fqTypeName, emptyR);
			// Intended blank
		}
		Matcher matcher = CodeVisitor.fqPattern.matcher(fqTypeName);
		if (!matcher.matches())
		{
			return null;
		}
		String packageName = matcher.group(1);
		String simpleName = matcher.group(2);
		Class<?> parentClass = loadClass(packageName);
		if (parentClass == null)
		{
			return null;
		}
		return loadClass(parentClass.getName() + "$" + simpleName);
	}

	protected FieldInfo mockField(JavaClassInfo owner, java.lang.reflect.Field field)
	{
		FieldInfo fi = new FieldInfo();
		StringBuilder tempSB = new StringBuilder();
		fi.setName(field.getName());
		if (field.getType().getPackage() != null)
		{
			tempSB.append(field.getType().getPackage().getName()).append('.');
		}
		tempSB.append(buildSimpleNameOfType(field.getType()));
		fi.setFieldType(tempSB.toString());
		fi.setOwningClass(owner);
		setModifiers(field.getModifiers(), fi);
		return fi;
	}

	protected void setModifiers(int modifiers, BaseJavaClassModelInfo model)
	{
		if (java.lang.reflect.Modifier.isFinal(modifiers))
		{
			model.setFinalFlag(true);
		}
		if (java.lang.reflect.Modifier.isStatic(modifiers))
		{
			model.setStaticFlag(true);
		}
		if (java.lang.reflect.Modifier.isPrivate(modifiers))
		{
			model.setPrivateFlag(true);
		}
		if (java.lang.reflect.Modifier.isProtected(modifiers))
		{
			model.setProtectedFlag(true);
		}
		if (java.lang.reflect.Modifier.isPublic(modifiers))
		{
			model.setPublicFlag(true);
		}
		if (java.lang.reflect.Modifier.isAbstract(modifiers))
		{
			model.setAbstractFlag(true);
		}
	}

	protected MethodInfo mockMethod(JavaClassInfo owner, java.lang.reflect.Method method)
	{
		StringBuilder tempSB = new StringBuilder();

		MethodInfo mi = new MethodInfo();
		mi.setName(method.getName());

		if (method.getReturnType().getPackage() != null)
		{
			tempSB.append(method.getReturnType().getPackage().getName()).append('.');
		}
		tempSB.append(buildSimpleNameOfType(method.getReturnType()));
		mi.setReturnType(tempSB.toString());
		mi.setOwningClass(owner);
		setModifiers(method.getModifiers(), mi);
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int a = 0, size = parameterTypes.length; a < size; a++)
		{
			final String parameterName = "arg" + a;
			Class<?> parameterType = parameterTypes[a];
			tempSB.setLength(0);
			while (parameterType.isArray())
			{
				parameterType = parameterType.getComponentType();
				tempSB.append("[]");
			}
			tempSB.insert(0, parameterType.getName());
			final String parameterTypeToString = tempSB.toString();

			mi.addParameters(new MockVariableElement(parameterName, parameterTypeToString));
		}
		return mi;
	}
}
