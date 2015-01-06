package de.osthus.esmeralda.handler.csharp;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.ILoggerHistory;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.TypeUsing;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IExpressionHandler;
import de.osthus.esmeralda.handler.IExpressionHandlerRegistry;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.uni.stmt.UniversalBlockHandler;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class CsHelper implements ICsHelper
{
	private static final HashSet<String> RESERVED_WORDS = new HashSet<>(Arrays.asList("abstract", "as", "base", "bool", "checked", "const", "decimal",
			"delegate", "event", "explicit", "extern", "fixed", "foreach", "goto", "implicit", "in", "internal", "is", "lock", "namespace", "object",
			"operator", "out", "override", "params", "readonly", "ref", "sbyte", "sealed", "sizeof", "stackalloc", "string", "struct", "typeof", "uint",
			"ulong", "unchecked", "unsafe", "ushort", "using", "virtual", "alias", "ascending", "async", "await", "descending", "dynamic", "from", "get",
			"global", "group", "into", "join", "let", "orderby", "partial", "select", "set", "var", "where", "yield"));

	protected static final HashMap<String, String[]> javaTypeToCsharpMap = new HashMap<String, String[]>();

	protected static final HashMap<String, String> implicitJavaImportsMap = new HashMap<String, String>();

	protected static final HashMap<String, String> annotationTargetMap = new HashMap<String, String>();

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

		put(java.io.InputStream.class.getName(), "System.IO.Stream");
		put(java.io.OutputStream.class.getName(), "System.IO.Stream");
		put(java.util.List.class.getName(), "System.Collections.Generic.IList");
		put(java.util.regex.Pattern.class.getName(), "System.Text.RegularExpressions.Regex");
		put(java.lang.annotation.Annotation.class.getName(), "System.Attribute");
		put(java.lang.annotation.Target.class.getName(), "System.AttributeUsageAttribute");
		put(java.lang.Class.class.getName(), "System.Type");
		put(java.lang.Class.class.getName() + "<?>", "System.Type");
		put(java.lang.Exception.class.getName(), "System.Exception");
		put(java.lang.IllegalArgumentException.class.getName(), "System.ArgumentException");
		put(java.lang.IllegalStateException.class.getName(), "System.Exception");
		put(java.lang.Throwable.class.getName(), "System.Exception");
		put(java.lang.Object.class.getName(), "System.Object");
		put(java.lang.StringBuilder.class.getName(), "System.Text.StringBuilder");
		put(java.lang.reflect.Field.class.getName(), "System.Reflection.FieldInfo");
		put(java.lang.RuntimeException.class.getName(), "System.Exception");
		put(java.lang.StackTraceElement.class.getName(), "System.Diagnostics.StackFrame");
		put(java.lang.ThreadLocal.class.getName(), "System.Threading.ThreadLocal", "De.Osthus.Ambeth.Util.ThreadLocal");
		put(de.osthus.ambeth.collections.IList.class.getName(), "System.Collections.Generic.IList");
		put(de.osthus.ambeth.collections.ArrayList.class.getName(), "System.Collections.Generic.List");
		put(de.osthus.ambeth.collections.HashSet.class.getName(), "De.Osthus.Ambeth.Collections.CHashSet");
		put(java.util.Map.Entry.class.getName(), "De.Osthus.Ambeth.Collections.Entry");

		annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.TYPE.name(), "Class");
		annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.PARAMETER.name(), "Parameter");

		// PACKAGE not supported in C#
		// annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.PACKAGE.name(), null);

		annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.METHOD.name(), "Method");

		// LOCAL_VARIABLE not supported in C#
		// annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.LOCAL_VARIABLE.name(), null);

		annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.FIELD.name(), "Field");
		annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.CONSTRUCTOR.name(), "Constructor");

		// ANNOTATION_TYPE not supported in C#
		// annotationTargetMap.put(ElementType.class.getName() + "." + ElementType.ANNOTATION_TYPE.name(), null);

		implicitJavaImportsMap.put("Boolean", Boolean.class.getName());
		implicitJavaImportsMap.put("Character", Character.class.getName());
		implicitJavaImportsMap.put("Byte", Byte.class.getName());
		implicitJavaImportsMap.put("Short", Short.class.getName());
		implicitJavaImportsMap.put("Integer", Integer.class.getName());
		implicitJavaImportsMap.put("Long", Long.class.getName());
		implicitJavaImportsMap.put("Float", Float.class.getName());
		implicitJavaImportsMap.put("Double", Double.class.getName());
		implicitJavaImportsMap.put("String", String.class.getName());
	}

	protected static final void put(String key, String... values)
	{
		javaTypeToCsharpMap.put(key, values);
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	@Autowired
	protected IExpressionHandlerRegistry expressionHandlerRegistry;

	@Override
	public void newLineIndent()
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
	public void preBlockWhiteSpaces()
	{
		// Intended blank
	}

	@Override
	public void postBlockWhiteSpaces()
	{
		newLineIndent();
	}

	@Override
	public void scopeIntend(IBackgroundWorkerDelegate run)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		newLineIndent();
		writer.append('{');
		context.incrementIndentationLevel();
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
			context.decrementIndentationLevel();
		}
		newLineIndent();
		writer.append('}');
	}

	@Override
	public void writeType(String typeName)
	{
		writeTypeIntern(typeName, false);
	}

	@Override
	public void writeTypeDirect(String typeName)
	{
		writeTypeIntern(typeName, true);
	}

	protected void writeTypeIntern(String typeName, boolean direct)
	{
		ParamChecker.assertParamNotNullOrEmpty(typeName, "typeName");
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		typeName = typeName.trim();
		String[] mappedTypeName = javaTypeToCsharpMap.get(typeName);
		if (mappedTypeName == null)
		{
			if (typeName.endsWith("[]"))
			{
				writeTypeIntern(typeName.substring(0, typeName.length() - 2), direct);
				writer.append("[]");
				return;
			}
			Matcher genericTypeMatcher = ASTHelper.genericTypePattern.matcher(typeName);
			if (genericTypeMatcher.matches())
			{
				String plainType = genericTypeMatcher.group(1);

				writeTypeIntern(plainType, direct);
				if (Class.class.getName().equals(plainType))
				{
					// in C# the type handle is not generic so we intentionally "lose" the generic type information here
					return;
				}
				writer.append('<');

				String typeArguments = genericTypeMatcher.group(2);
				String[] typeArgumentsSplit = astHelper.splitTypeArgument(typeArguments);
				boolean firstArgument = true;
				for (String typeArgumentSplit : typeArgumentsSplit)
				{
					firstArgument = writeStringIfFalse(",", firstArgument);
					writeTypeIntern(typeArgumentSplit, direct);
				}
				writer.append('>');
				return;
			}
			if (!direct)
			{
				typeName = astHelper.resolveFqTypeFromTypeName(typeName);
				mappedTypeName = camelCaseName(new String[] { typeName });
			}
			else
			{
				mappedTypeName = new String[] { typeName };
			}
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
	public boolean newLineIndentIfFalse(boolean value)
	{
		if (!value)
		{
			newLineIndent();
		}
		return false;
	}

	@Override
	public void writeSimpleName(JavaClassInfo classInfo)
	{
		throw new UnsupportedOperationException("Not yet implemented");
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
		String namespace = createNamespace();
		String relativeTargetPathName = namespace.replace(".", File.separator);

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
		String nonGenericType = astHelper.extractNonGenericType(classInfo.getName());
		return nonGenericType + ".cs";
	}

	@Override
	public String createNamespace()
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo classInfo = context.getClassInfo();
		String packageName = classInfo.getPackageName();

		String namespace = createNamespace(packageName);

		return namespace;
	}

	@Override
	public String createNamespace(String packageName)
	{
		IConversionContext context = this.context.getCurrent();

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

		String[] packageSplit = packageName.split(Pattern.quote("."));
		packageSplit = camelCaseName(packageSplit);
		String namespace = StringConversionHelper.implode(objectCollector, packageSplit, ".");

		return namespace;
	}

	protected String[] camelCaseName(String[] strings)
	{
		String[] camelCase = new String[strings.length];
		for (int a = strings.length; a-- > 0;)
		{
			camelCase[a] = StringConversionHelper.upperCaseFirst(objectCollector, strings[a]);
		}
		return camelCase;
	}

	@Override
	public String createMethodName(String methodName)
	{
		methodName = StringConversionHelper.upperCaseFirst(objectCollector, methodName);
		return methodName;
	}

	@Override
	public void startDocumentation()
	{
		// No special symbols to start a doc block in C#
	}

	@Override
	public void newLineIndentDocumentation()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		newLineIndent();
		writer.append("/// ");
	}

	@Override
	public void endDocumentation()
	{
		// No special symbols to end a doc block in C#
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
			writeExpressionTree(arg);
		}
		writer.append(')');
	}

	@Override
	public void writeMethodArguments(JCExpression methodInvocation)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		writer.append(methodInvocation.toString());
	}

	@Override
	public void writeAnnotation(Annotation annotation)
	{
		if (SuppressWarnings.class.getName().equals(annotation.getType()))
		{
			// skip this annotation
			return;
		}
		if (Retention.class.getName().equals(annotation.getType()))
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
		newLineIndent();
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
		if (Target.class.getName().equals(annotation.getType()))
		{
			AnnotationValue valueOfValue = properties.remove("value");
			if (valueOfValue != null)
			{
				Attribute[] values = ((Attribute.Array) valueOfValue).values;
				firstProperty = writeStringIfFalse(", ", firstProperty);

				boolean firstAttributeTarget = true;
				// in C# the ValidOn value can be passed directly as a constructor argument without key=value pattern
				for (int a = 0, size = values.length; a < size; a++)
				{
					Attribute value = values[a];
					String attributeTarget = annotationTargetMap.get(value.toString());
					if (attributeTarget == null)
					{
						continue;
					}
					firstAttributeTarget = writeStringIfFalse(" | ", firstAttributeTarget);
					writeTypeDirect("System.AttributeTargets");
					writer.append('.').append(attributeTarget);
				}
			}
			properties.put("inherited", new AnnotationValue()
			{
				@Override
				public Object getValue()
				{
					return "false";
				}

				@Override
				public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public String toString()
				{
					return "false";
				}
			});
			properties.put("allowMultiple", new AnnotationValue()
			{
				@Override
				public Object getValue()
				{
					return "false";
				}

				@Override
				public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public String toString()
				{
					return "false";
				}
			});
		}
		for (Entry<String, AnnotationValue> entry : properties)
		{
			firstProperty = writeStringIfFalse(", ", firstProperty);
			String propertyName = StringConversionHelper.upperCaseFirst(objectCollector, entry.getKey());
			writer.append(propertyName).append(" = ");
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

	@Override
	public void writeVariableName(String varName)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		if (RESERVED_WORDS.contains(varName))
		{
			varName += "_";
		}

		writer.append(varName);
	}

	@Override
	public void writeExpressionTree(Tree expression)
	{
		if (expression == null)
		{
			return;
		}
		Kind kind = expression.getKind();
		IExpressionHandler expressionHandler = expressionHandlerRegistry.getExtension(Lang.C_SHARP + kind);
		if (expressionHandler != null)
		{
			expressionHandler.handleExpression(expression);
			return;
		}
		handleChildStatement(expression);
	}

	protected void handleChildStatement(Tree statement)
	{
		handleChildStatement(statement, true);
	}

	protected <T extends Tree> void handleChildStatement(T statement, boolean standalone)
	{
		IConversionContext context = this.context.getCurrent();
		ISnippetManager snippetManager = context.getSnippetManager();

		Kind kind = statement.getKind();
		IStatementHandlerExtension<Tree> stmtHandler = statementHandlerRegistry.getExtension(Lang.C_SHARP + kind);
		if (stmtHandler != null && stmtHandler.getClass().equals(UniversalBlockHandler.class))
		{
			stmtHandler.handle(statement, standalone);
		}
		else if (stmtHandler != null)
		{
			context.incrementIndentationLevel();
			stmtHandler.handle(statement, standalone);
			context.decrementIndentationLevel();
		}
		else if (standalone)
		{
			String statementString = statement.toString();
			List<String> untranslatableStatements = Collections.singletonList(statementString);
			snippetManager.writeSnippet(untranslatableStatements);
		}
		else
		{
			throw new IllegalArgumentException("Cannot handle embedded statement " + statement.toString());
		}
	}
}
