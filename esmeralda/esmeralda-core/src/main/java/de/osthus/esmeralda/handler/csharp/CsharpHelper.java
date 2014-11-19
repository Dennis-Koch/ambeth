package de.osthus.esmeralda.handler.csharp;

import java.io.File;
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
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.ConversionContext;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
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
		put(java.lang.Void.class.getName(), "void");
		put(java.lang.Boolean.class.getName(), "bool?");
		put(java.lang.Character.class.getName(), "char?");
		put(java.lang.Byte.class.getName(), "sbyte?");
		put(java.lang.Short.class.getName(), "short?");
		put(java.lang.Integer.class.getName(), "int?");
		put(java.lang.Long.class.getName(), "long?");
		put(java.lang.Float.class.getName(), "float?");
		put(java.lang.Double.class.getName(), "double?");
		put(java.lang.String.class.getName(), "System.String");

		put(java.util.List.class.getName(), "System.Collections.Generic.IList");
		put(java.lang.Class.class.getName(), "System.Type");
		put(java.lang.Class.class.getName() + "<?>", "System.Type");
		put(java.lang.StringBuilder.class.getName(), "System.Text.StringBuilder");
		put(java.lang.ThreadLocal.class.getName(), "System.Threading.ThreadLocal", "De.Osthus.Ambeth.Util.ThreadLocal");
		put(de.osthus.ambeth.collections.IList.class.getName(), "System.Collections.Generic.IList");
		put(de.osthus.ambeth.collections.ArrayList.class.getName(), "System.Collections.Generic.List");
		put(de.osthus.ambeth.collections.HashSet.class.getName(), "De.Osthus.Ambeth.Collections.CHashSet");
		put(java.util.Map.Entry.class.getName(), "De.Osthus.Ambeth.Collections.Entry");
	}

	protected static final void put(String key, String... values)
	{
		javaTypeToCsharpMap.put(key, values);
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void newLineIntend()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		writer.append('\n');
		int indentationLevel = context.getIndentationLevel();
		for (int a = indentationLevel; a-- > 0;)
		{
			writer.append('\t');
		}
	}

	@Override
	public void scopeIntend(IBackgroundWorkerDelegate run)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		newLineIntend();
		writer.append('{');
		context.incremetIndentationLevel();
		try
		{
			run.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			context.decremetIndentationLevel();
		}
		newLineIntend();
		writer.append('}');
	}

	@Override
	public void writeType(String typeName)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		typeName = typeName.trim();
		String[] mappedTypeName = javaTypeToCsharpMap.get(typeName);
		if (mappedTypeName == null)
		{
			if (typeName.endsWith("[]"))
			{
				writeType(typeName.substring(0, typeName.length() - 2));
				writer.append("[]");
				return;
			}
			Matcher genericTypeMatcher = ConversionContext.genericTypePattern.matcher(typeName);
			if (genericTypeMatcher.matches())
			{
				String plainType = genericTypeMatcher.group(1);

				writeType(plainType);
				if (Class.class.getName().equals(plainType))
				{
					// in C# the type handle is not generic so we intentionally "lose" the generic type information here
					return;
				}
				writer.append('<');

				String typeArguments = genericTypeMatcher.group(2);
				String[] typeArgumentsSplit = commaSplitPattern.split(typeArguments);
				boolean firstArgument = true;
				for (String typeArgumentSplit : typeArgumentsSplit)
				{
					firstArgument = writeStringIfFalse(",", firstArgument);
					writeType(typeArgumentSplit);
				}
				writer.append('>');
				return;
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
	}

	@Override
	public boolean writeStringIfFalse(String value, boolean condition)
	{

		if (!condition)
		{
			IConversionContext context = this.context.getCurrent();
			IWriter writer = context.getWriter();
			writer.append(value);
		}
		return false;
	}

	@Override
	public boolean newLineIntendIfFalse(boolean value)
	{
		if (!value)
		{
			newLineIntend();
		}
		return false;
	}

	@Override
	public File createTargetFile()
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo classInfo = context.getClassInfo();
		File targetPath = context.getTargetPath();
		Path relativeTargetPath = createRelativeTargetPath();
		File targetFileDir = new File(targetPath, relativeTargetPath.toString());
		targetFileDir.mkdirs();

		File targetFile = new File(targetFileDir, createTargetFileName(classInfo));
		return targetFile;
	}

	@Override
	public Path createRelativeTargetPath()
	{
		IConversionContext context = this.context.getCurrent();
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

		String relativeTargetPathName = packageName.replace(".", File.separator);

		String languagePath = context.getLanguagePath();
		if (languagePath != null && !languagePath.isEmpty())
		{
			relativeTargetPathName = languagePath + File.separator + relativeTargetPathName;
		}
		Path relativeTargetPath = Paths.get(relativeTargetPathName);

		return relativeTargetPath;
	}

	@Override
	public String createTargetFileName(JavaClassInfo classInfo)
	{
		return classInfo.getName() + ".cs";
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
	public void writeAnnotations(BaseJavaClassModel model)
	{
		IList<Annotation> annotations = model.getAnnotations();
		for (int a = 0, size = annotations.size(); a < size; a++)
		{
			Annotation annotation = annotations.get(a);
			writeAnnotation(annotation);
		}
	}

	@Override
	public void writeNewInstance(JCNewClass newClass)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		List<JCExpression> arguments = newClass.args;
		List<Type> genericTypeArguments = newClass.type != null ? newClass.type.allparams() : null;
		// List<Type> argumentTypes = ((MethodType) newClass.constructor.type).getTypeArguments();
		String owner = newClass.constructor != null ? ((ClassSymbol) newClass.constructor.owner).fullname.toString() : newClass.clazz.toString();

		writer.append(" new ");
		writeType(owner);

		writeGenericTypeArguments(genericTypeArguments);
		writeMethodArguments(arguments);
	}

	@Override
	public void writeGenericTypeArguments(List<Type> genericTypeArguments)
	{
		if (genericTypeArguments == null || genericTypeArguments.size() == 0)
		{
			return;
		}
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		writer.append('<');
		for (int a = 0, size = genericTypeArguments.size(); a < size; a++)
		{
			Type genericTypeArgument = genericTypeArguments.get(a);
			if (a > 0)
			{
				writer.append(", ");
			}
			writeType(genericTypeArgument.toString());
		}
		writer.append('>');
	}

	@Override
	public void writeMethodArguments(List<JCExpression> methodArguments)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
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
	public void writeAnnotation(Annotation annotation)
	{
		if (SuppressWarnings.class.getName().equals(annotation.getType()))
		{
			// skip this annotation
			return;
		}
		if (Override.class.getName().equals(annotation.getType()))
		{
			// skip this annotation because overrides of interfaces is NOT an override in C# sense. So we need to check for overridden abstract or concrete
			// methods from superclasses to write a C# override
			return;
		}
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		newLineIntend();
		writer.append('[');
		writeType(annotation.getType());
		IMap<String, AnnotationValue> properties = annotation.getProperties();
		if (properties.size() == 0)
		{
			writer.append(']');
			return;
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
				firstProperty = writeStringIfFalse(", ", firstProperty);
				writer.append(valueOfName.toString());
			}
		}
		for (Entry<String, AnnotationValue> entry : properties)
		{
			firstProperty = writeStringIfFalse(", ", firstProperty);
			String propertyName = StringConversionHelper.upperCaseFirst(objectCollector, entry.getKey());
			writer.append(propertyName).append("=");
			writer.append(entry.getValue().toString());
		}
		writer.append(')');
	}

	@Override
	public boolean writeModifiers(BaseJavaClassModel javaClassModel)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		boolean firstKeyWord = true;
		if (javaClassModel.isPrivate())
		{
			firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
			writer.append("private");
		}
		else if (javaClassModel.isProtected())
		{
			firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
			writer.append("protected");
		}
		else if (javaClassModel.isPublic())
		{
			firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
			writer.append("public");
		}
		if (javaClassModel.isAbstract())
		{
			firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
			writer.append("abstract");
		}
		if (javaClassModel.isStatic())
		{
			firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
			writer.append("static");
		}
		if (javaClassModel.isFinal())
		{
			firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
			if (javaClassModel instanceof JavaClassInfo)
			{
				writer.append("sealed");
			}
			else
			{
				writer.append("readonly");
			}
		}
		return firstKeyWord;
	}
}
