package de.osthus.esmeralda.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.EmptySet;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.esmeralda.IConversionContext;
import demo.codeanalyzer.common.model.BaseJavaClassModelInfo;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.MethodInfo;
import demo.codeanalyzer.helper.ClassInfoDataSetter;

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
	protected IConversionContext context;

	@Override
	public JavaClassInfo createClassInfo(String fqName)
	{
		if (fqName.endsWith("[]"))
		{
			String componentName = fqName.substring(0, fqName.length() - 2);
			JavaClassInfo componentCI = context.resolveClassInfo(componentName, true);
			if (componentCI == null)
			{
				return null;
			}
			JavaClassInfo ci = new JavaClassInfo(context);
			ci.setArray(true);
			ci.setPackageName(componentCI.getPackageName());
			ci.setName(componentCI.getName() + "[]");

			FieldInfo lengthField = new FieldInfo();
			lengthField.setOwningClass(ci);
			lengthField.setName("length");
			lengthField.setFieldType("int");
			ci.addField(lengthField);
			return ci;
		}
		JavaClassInfo classInfo = new JavaClassInfo(context);

		Class<?> type = loadClass(fqName);
		if (type == null)
		{
			return null;
		}
		classInfo.setName(type.getSimpleName());
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

	protected Class<?> loadClass(String fqTypeName)
	{
		Class<?> nativeType = nativeTypesSet.get(fqTypeName);
		if (nativeType != null)
		{
			return nativeType;
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
				return classLoader.loadClass("java.lang." + fqTypeName);
			}
		}
		catch (Throwable e)
		{
			// Intended blank
		}
		Matcher matcher = ClassInfoDataSetter.fqPattern.matcher(fqTypeName);
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
		fi.setName(field.getName());
		fi.setFieldType(field.getType().getName());
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
		MethodInfo mi = new MethodInfo();
		mi.setName(method.getName());
		mi.setReturnType(method.getReturnType().getName());
		mi.setOwningClass(owner);
		setModifiers(method.getModifiers(), mi);
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int a = 0, size = parameterTypes.length; a < size; a++)
		{
			final String parameterName = "arg" + a;
			final Class<?> parameterType = parameterTypes[a];
			VariableElement ve = new VariableElement()
			{
				@Override
				public Name getSimpleName()
				{
					return new Name()
					{
						@Override
						public CharSequence subSequence(int start, int end)
						{
							return parameterName.subSequence(start, end);
						}

						@Override
						public int length()
						{
							return parameterName.length();
						}

						@Override
						public char charAt(int index)
						{
							return parameterName.charAt(index);
						}

						@Override
						public boolean contentEquals(CharSequence cs)
						{
							return parameterName.contentEquals(cs);
						}

						@Override
						public String toString()
						{
							return parameterName;
						}
					};
				}

				@Override
				public Set<Modifier> getModifiers()
				{
					return EmptySet.emptySet();
				}

				@Override
				public ElementKind getKind()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public Element getEnclosingElement()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public List<? extends Element> getEnclosedElements()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public List<? extends AnnotationMirror> getAnnotationMirrors()
				{
					return EmptyList.getInstance();
				}

				@Override
				public <A extends Annotation> A getAnnotation(Class<A> annotationType)
				{
					return null;
				}

				@Override
				public TypeMirror asType()
				{
					return new TypeMirror()
					{
						@Override
						public TypeKind getKind()
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public <R, P> R accept(TypeVisitor<R, P> v, P p)
						{
							throw new UnsupportedOperationException();
						}

						@Override
						public String toString()
						{
							return parameterType.getName();
						}
					};
				}

				@Override
				public <R, P> R accept(ElementVisitor<R, P> v, P p)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public Object getConstantValue()
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public String toString()
				{
					return asType().toString() + " " + getSimpleName().toString();
				}
			};
			mi.addParameters(ve);
		}
		return mi;
	}
}
