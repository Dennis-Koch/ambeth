package de.osthus.esmeralda.handler.csharp;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationValue;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.TypeUsing;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class CsharpHelper implements ICsharpHelper
{
	protected static final Pattern commaSplitPattern = Pattern.compile(",");

	protected static final HashMap<String, String[]> javaTypeToCsharpMap = new HashMap<String, String[]>();

	static
	{
		put("void", "void");
		put("boolean", "bool");
		put("char", "char");
		put("byte", "sbyte");
		put("short", "short");
		put("int", "int");
		put("long", "long");
		put("float", "float");
		put("double", "double");
		put(Void.class.getName(), "void");
		put(Boolean.class.getName(), "bool?");
		put(Character.class.getName(), "char?");
		put(Byte.class.getName(), "sbyte?");
		put(Short.class.getName(), "short?");
		put(Integer.class.getName(), "int?");
		put(Long.class.getName(), "long?");
		put(Float.class.getName(), "float?");
		put(Double.class.getName(), "double?");
		put(String.class.getName(), "System.String");
		put("java.lang.Class<?>", "System.Type");
		put(ThreadLocal.class.getName(), "System.Threading.ThreadLocal", "De.Osthus.Ambeth.Util.ThreadLocal");

		put(java.util.List.class.getName(), "System.Collections.Generic.IList");
		put(de.osthus.ambeth.collections.IList.class.getName(), "System.Collections.Generic.IList");
		put(de.osthus.ambeth.collections.ArrayList.class.getName(), "System.Collections.Generic.List");
		put(de.osthus.ambeth.collections.HashSet.class.getName(), "De.Osthus.Ambeth.Collections.CHashSet");
		put("java.util.Map.Entry", "De.Osthus.Ambeth.Collections.Entry");
	}

	protected static final void put(String key, String... values)
	{
		javaTypeToCsharpMap.put(key, values);
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public Writer newLineIntend(ConversionContext context, Writer writer) throws IOException
	{
		writer.append('\n');
		int indentationLevel = context.getIndentationLevel();
		for (int a = indentationLevel; a-- > 0;)
		{
			writer.append('\t');
		}
		return writer;
	}

	@Override
	public void scopeIntend(ConversionContext context, Writer writer, IBackgroundWorkerDelegate run) throws Throwable
	{
		newLineIntend(context, writer).append('{');
		context.incremetIndentationLevel();
		try
		{
			run.invoke();
		}
		finally
		{
			context.decremetIndentationLevel();
		}
		newLineIntend(context, writer).append('}');
	}

	@Override
	public Writer writeType(String typeName, ConversionContext context, Writer writer) throws Throwable
	{
		typeName = typeName.trim();
		String[] mappedTypeName = javaTypeToCsharpMap.get(typeName);
		if (mappedTypeName == null)
		{
			if (typeName.endsWith("[]"))
			{
				writeType(typeName.substring(0, typeName.length() - 2), context, writer);
				writer.append("[]");
				return writer;
			}
			Matcher genericTypeMatcher = ConversionContext.genericTypePattern.matcher(typeName);
			if (genericTypeMatcher.matches())
			{
				String plainType = genericTypeMatcher.group(1);

				writeType(plainType, context, writer).append('<');

				String typeArguments = genericTypeMatcher.group(2);
				String[] typeArgumentsSplit = commaSplitPattern.split(typeArguments);
				boolean firstArgument = true;
				for (String typeArgumentSplit : typeArgumentsSplit)
				{
					if (firstArgument)
					{
						firstArgument = false;
					}
					else
					{
						writer.append(',');
					}
					writeType(typeArgumentSplit, context, writer);
				}
				writer.append('>');
				return writer;
			}
			mappedTypeName = camelCaseName(new String[] { typeName });
		}
		ISet<TypeUsing> usedTypes = context.getUsedTypes();
		if (usedTypes != null)
		{
			usedTypes.add(new TypeUsing(mappedTypeName[0], false));
			if (mappedTypeName.length > 1)
			{
				// TypeUsing silverlightTypeUsing = new TypeUsing(mappedTypeName[1], true);
				// TypeUsing existingTypeUsing = usedTypes.get(silverlightTypeUsing);
				// if (existingTypeUsing == null)
				// {
				// // add silverlight using only if it is not already added for non-silverlight
				// usedTypes.add(silverlightTypeUsing);
				// }
			}
		}
		else
		{
			Map<String, String> imports = context.getImports();
			if (imports != null)
			{
				String nameFromImplicitImport = imports.get(mappedTypeName[0]);
				if (nameFromImplicitImport != null)
				{
					mappedTypeName = new String[] { nameFromImplicitImport };
				}
			}
		}
		writer.append(mappedTypeName[0]);
		return writer;
	}

	@Override
	public boolean writeStringIfFalse(String value, boolean condition, ConversionContext context, Writer writer) throws Throwable
	{
		if (!condition)
		{
			writer.append(value);
		}
		return false;
	}

	@Override
	public boolean newLineIntendIfFalse(boolean value, ConversionContext context, Writer writer) throws Throwable
	{
		if (!value)
		{
			newLineIntend(context, writer);
		}
		return false;
	}

	@Override
	public File createTargetFile(ConversionContext context)
	{
		JavaClassInfo classInfo = context.getClassInfo();
		String packageName = classInfo.getPackageName();

		String nsPrefixRemove = context.getNsPrefixRemove();
		if (packageName.startsWith(nsPrefixRemove))
		{
			int removeLength = nsPrefixRemove.length();
			packageName = packageName.substring(removeLength);
		}

		String nsPrefixAdd = context.getNsPrefixAdd();
		if (nsPrefixAdd != null)
		{
			packageName = nsPrefixAdd + packageName;
		}

		packageName = camelCaseName(packageName);
		packageName = packageName.replace(".", "/");

		File targetPath = context.getTargetPath();
		Path targetFilePath = Paths.get(targetPath.getPath());
		String languagePath = context.getLanguagePath();
		if (languagePath != null && !languagePath.isEmpty())
		{
			targetFilePath = targetFilePath.resolve(languagePath);
		}
		File targetFileDir = new File(targetFilePath.toFile(), packageName);
		targetFileDir.mkdirs();

		File targetFile = new File(targetFileDir, classInfo.getName() + ".cs");
		return targetFile;
	}

	@Override
	public String camelCaseName(String typeName)
	{
		String[] packageSplit = typeName.split(Pattern.quote("."));
		StringBuilder sb = new StringBuilder();
		for (int a = 0, size = packageSplit.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append('.');
			}
			sb.append(StringConversionHelper.upperCaseFirst(objectCollector, packageSplit[a]));
		}
		return sb.toString();
	}

	protected String[] camelCaseName(String[] typeNames)
	{
		String[] camelCase = new String[typeNames.length];
		for (int a = typeNames.length; a-- > 0;)
		{
			camelCase[a] = camelCaseName(typeNames[a]);
		}
		return camelCase;
	}

	@Override
	public Writer writeAnnotations(BaseJavaClassModel model, ConversionContext context, Writer writer) throws Throwable
	{
		IList<Annotation> annotations = model.getAnnotations();
		for (int a = 0, size = annotations.size(); a < size; a++)
		{
			Annotation annotation = annotations.get(a);
			writeAnnotation(annotation, context, writer);
		}
		return writer;
	}

	@Override
	public Writer writeNewInstance(JCNewClass newClass, ConversionContext context, Writer writer) throws Throwable
	{
		List<JCExpression> arguments = newClass.args;
		List<Type> genericTypeArguments = newClass.type != null ? newClass.type.allparams() : null;
		// List<Type> argumentTypes = ((MethodType) newClass.constructor.type).getTypeArguments();
		String owner = newClass.constructor != null ? ((ClassSymbol) newClass.constructor.owner).fullname.toString() : newClass.clazz.toString();

		writer.append(" = new ");
		writeType(owner, context, writer);

		writeGenericTypeArguments(genericTypeArguments, context, writer);
		writeMethodArguments(arguments, context, writer);
		return writer;
	}

	@Override
	public Writer writeGenericTypeArguments(List<Type> genericTypeArguments, ConversionContext context, Writer writer) throws Throwable
	{
		if (genericTypeArguments == null || genericTypeArguments.size() == 0)
		{
			return writer;
		}
		writer.append('<');
		for (int a = 0, size = genericTypeArguments.size(); a < size; a++)
		{
			Type genericTypeArgument = genericTypeArguments.get(a);
			if (a > 0)
			{
				writer.append(", ");
			}
			writeType(genericTypeArgument.toString(), context, writer);
		}
		writer.append('>');
		return writer;
	}

	@Override
	public Writer writeMethodArguments(List<JCExpression> methodArguments, ConversionContext context, Writer writer) throws Throwable
	{
		writer.append('(');
		for (int a = 0, size = methodArguments.size(); a < size; a++)
		{
			JCExpression arg = methodArguments.get(a);
			if (a > 0)
			{
				writer.append(", ");
			}
			writer.append(arg.toString());
		}
		writer.append(')');
		return writer;
	}

	@Override
	public boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType, ConversionContext context) throws Throwable
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
	public Writer writeAnnotation(Annotation annotation, ConversionContext context, Writer writer) throws Throwable
	{
		if (SuppressWarnings.class.getName().equals(annotation.getType()))
		{
			// skip this annotation
			return writer;
		}
		if (Override.class.getName().equals(annotation.getType()))
		{
			// skip this annotation because overrides of interfaces is NOT an override in C# sense. So we need to check for overridden abstract or concrete
			// methods from superclasses to write a C# override
			return writer;
		}
		newLineIntend(context, writer);
		writer.append('[');
		writeType(annotation.getType(), context, writer);
		IMap<String, AnnotationValue> properties = annotation.getProperties();
		if (properties.size() == 0)
		{
			writer.append(']');
			return writer;
		}
		writer.append('(');
		// clone the map to be able to modify it
		properties = new LinkedHashMap<String, AnnotationValue>(properties);
		boolean firstProperty = true;
		if (Property.class.getName().equals(annotation.getType()))
		{
			AnnotationValue valueOfName = properties.remove("name");
			if (valueOfName != null)
			{
				// in C# the name value can be passed directly as a constructor argument without key=value pattern
				firstProperty = writeStringIfFalse(", ", firstProperty, context, writer);
				writer.append(valueOfName.toString());
			}
		}
		for (Entry<String, AnnotationValue> entry : properties)
		{
			firstProperty = writeStringIfFalse(", ", firstProperty, context, writer);
			String propertyName = StringConversionHelper.upperCaseFirst(objectCollector, entry.getKey());
			writer.append(propertyName).append("=");
			writer.append(entry.getValue().toString());
		}
		writer.append(')');
		return writer;
	}

	@Override
	public boolean writeModifiers(BaseJavaClassModel javaClassModel, ConversionContext context, Writer writer) throws Throwable
	{
		boolean firstKeyWord = true;
		if (javaClassModel.isPrivate())
		{
			writer.append("private");
			firstKeyWord = false;
		}
		else if (javaClassModel.isProtected())
		{
			writer.append("protected");
			firstKeyWord = false;
		}
		else if (javaClassModel.isPublic())
		{
			writer.append("public");
			firstKeyWord = false;
		}
		if (javaClassModel.isAbstract())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("abstract");
		}
		if (javaClassModel.isStatic())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("static");
		}
		if (javaClassModel.isFinal())
		{
			if (firstKeyWord)
			{
				firstKeyWord = false;
			}
			else
			{
				writer.append(' ');
			}
			writer.append("readonly");
		}
		return firstKeyWord;
	}
}
