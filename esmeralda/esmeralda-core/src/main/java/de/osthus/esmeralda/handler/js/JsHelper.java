package de.osthus.esmeralda.handler.js;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
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
import de.osthus.esmeralda.misc.IToDoWriter;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.FieldInfo;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;
import demo.codeanalyzer.common.model.MethodInfo;

public class JsHelper implements IJsHelper
{
	private static final String BOOLEAN = "Boolean";
	private static final String NUMBER = "Number";
	private static final String OBJECT = "Object";
	private static final String STRING = "String";

	private static final Pattern GENERIC_NAME = Pattern.compile("[A-Z]");

	private static final HashSet<String> RESERVED_WORDS = new HashSet<>(Arrays.asList("arguments", "const", "debugger", "default", "delete", "eval", "export",
			"function", "goto", "import", "in", "let", "native", "transient", "typeof", "var", "volatile", "with", "yield", "Infinity", "isFinite", "isNaN",
			"isPrototypeOf", "NaN", "prototype", "undefined", "valueOf"));

	protected static final HashMap<String, String[]> javaTypeToJsMap = new HashMap<String, String[]>();

	static
	{
		put("void", "void");
		put("boolean", BOOLEAN);
		put("char", STRING);
		put("byte", NUMBER);
		put("short", NUMBER);
		put("int", NUMBER);
		put("long", NUMBER);
		put("float", NUMBER);
		put("double", NUMBER);
		put(java.lang.Void.class.getName(), "void");
		// put(java.lang.Boolean.class.getName(), BOOLEAN);
		// put(java.lang.Character.class.getName(), STRING);
		// put(java.lang.Byte.class.getName(), NUMBER);
		// put(java.lang.Short.class.getName(), NUMBER);
		// put(java.lang.Integer.class.getName(), NUMBER);
		// put(java.lang.Long.class.getName(), NUMBER);
		// put(java.lang.Float.class.getName(), NUMBER);
		// put(java.lang.Double.class.getName(), NUMBER);
		put(java.lang.String.class.getName(), STRING);

		// put(java.io.InputStream.class.getName(), "System.IO.Stream");
		// put(java.io.OutputStream.class.getName(), "System.IO.Stream");
		put(java.util.List.class.getName(), "Ambeth.collections.IList");
		// put(java.util.regex.Pattern.class.getName(), "System.Text.RegularExpressions.Regex");
		// put(java.lang.annotation.Annotation.class.getName(), "System.Attribute");
		// put(java.lang.annotation.Target.class.getName(), "System.AttributeUsageAttribute");
		// put(java.lang.Class.class.getName(), "System.Type");
		// put(java.lang.Class.class.getName() + "<?>", "System.Type");
		// put(java.lang.Exception.class.getName(), "System.Exception");
		// put(java.lang.IllegalArgumentException.class.getName(), "System.ArgumentException");
		// put(java.lang.IllegalStateException.class.getName(), "System.Exception");
		// put(java.lang.Throwable.class.getName(), "System.Exception");
		put(java.lang.Object.class.getName(), OBJECT);
		// put(java.lang.StringBuilder.class.getName(), "System.Text.StringBuilder");
		// put(java.lang.reflect.Field.class.getName(), "System.Reflection.FieldInfo");
		// put(java.lang.RuntimeException.class.getName(), "System.Exception");
		// put(java.lang.StackTraceElement.class.getName(), "System.Diagnostics.StackFrame");
		put(java.lang.ThreadLocal.class.getName(), "Ambeth.util.ThreadLocal");
		put(de.osthus.ambeth.collections.IList.class.getName(), "Ambeth.collections.IList");
		put(de.osthus.ambeth.collections.HashSet.class.getName(), "Ambeth.collections.CHashSet");
		put(java.util.Map.Entry.class.getName(), "Ambeth.collections.Entry");
	}

	protected static final void put(String key, String... values)
	{
		javaTypeToJsMap.put(key, values);
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IASTHelper astHelper;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IStatementHandlerRegistry statementHandlerRegistry;

	@Autowired
	protected IExpressionHandlerRegistry expressionHandlerRegistry;

	@Autowired
	protected IToDoWriter toDoWriter;

	@Override
	public JsSpecific getLanguageSpecific()
	{
		IConversionContext context = this.context.getCurrent();
		JsSpecific jsSpecific = (JsSpecific) context.getLanguageSpecific();
		return jsSpecific;
	}

	@Override
	public void newLineIndent()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append('\n');
		int indentationLevel = context.getIndentationLevel();
		for (int a = indentationLevel; a-- > 0;)
		{
			writer.append("    ");
		}
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
	public boolean newLineIndentWithCommaIfFalse(boolean value)
	{
		if (!value)
		{
			IConversionContext context = this.context.getCurrent();
			IWriter writer = context.getWriter();

			writer.append(',');
			newLineIndent();
		}
		return false;
	}

	@Override
	public void preBlockWhiteSpaces()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append(' ');
	}

	@Override
	public void postBlockWhiteSpaces()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append(' ');
	}

	@Override
	public void scopeIntend(IBackgroundWorkerDelegate run)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
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
		return nonGenericType + ".js";
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
		packageName = prefixModification(packageName, context);
		String namespace = StringConversionHelper.upperCaseFirst(objectCollector, packageName);

		return namespace;
	}

	@Override
	public String createMethodName(String methodName)
	{
		return methodName;
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
	public void writeSimpleName(final JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String className = classInfo.getName().split("<", 2)[0];
		writer.append(className);
	}

	@Override
	public void writeAsTypeOf(String typeName)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append("Ambeth.forName('");
		writeTypeIntern(typeName, false);
		writer.append("')");
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

	@Override
	public void startDocumentation()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		newLineIndent();
		writer.append("/**");
	}

	@Override
	public void newLineIndentDocumentation()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		newLineIndent();
		writer.append(" * ");
	}

	@Override
	public void endDocumentation()
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		newLineIndent();
		writer.append(" */");
	}

	@Override
	public boolean writeModifiers(BaseJavaClassModel javaClassModel)
	{
		return false;
	}

	@Override
	public void writeMetadata(BaseJavaClassModel model)
	{
		if (!(model instanceof FieldInfo) && !(model instanceof MethodInfo))
		{
			return;
		}

		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String prefix = model instanceof FieldInfo ? "m$p_" : "m$f_";
		String name = (model instanceof MethodInfo && ((MethodInfo) model).isConstructor()) ? "constructor" : model.getName();

		newLineIndentWithCommaIfFalse(false);
		writer.append('"').append(prefix).append(name).append("\": {");

		String type = (model instanceof FieldInfo) ? ((FieldInfo) model).getFieldType() : ((MethodInfo) model).getReturnType();
		writeMetadataType(type, writer);

		// writer.append(", \"modifier\": \"").append(access).append('"');

		writeAnnotations(model);

		writer.append('}');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeMetadata(String methodName, String returnType, ArrayList<Method> methods)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		newLineIndentWithCommaIfFalse(false);
		writer.append("\"m$f_").append(methodName).append("\": {");
		writeMetadataType(returnType, writer);
		writer.append(", \"overloads\": [");

		ArrayList<Method>[] methodBuckets = bucketSortMethods(methods);

		boolean ambiguousParameterNames = false;

		boolean firstBucket = true;
		context.incrementIndentationLevel();
		for (int i = 0, length = methodBuckets.length; i < length; i++)
		{
			final ArrayList<Method> bucket = methodBuckets[i];
			if (bucket == null)
			{
				firstBucket = writeStringIfFalse(",", firstBucket);
				newLineIndent();
				writer.append("null");
			}
			else
			{
				HashMap<String, Method> paramNamesMaps = new HashMap<>();

				boolean singleMethod = bucket.size() == 1;
				firstBucket = writeStringIfFalse(",", firstBucket);
				newLineIndent();
				writer.append("[");
				boolean firstMethod = true;
				context.incrementIndentationLevel();
				for (Method method : bucket)
				{
					String[] paramNames = new String[i];

					String methodNamePostfix = createOverloadedMethodNamePostfix(method.getParameters());
					String fullMethodName = methodName + methodNamePostfix;
					firstMethod = writeStringIfFalse(",", firstMethod);
					newLineIndentIfFalse(singleMethod);
					writer.append("{ \"methodName\": \"").append(fullMethodName).append("\", ");
					writer.append("\"methodInstance\": this.").append(fullMethodName).append(", ");
					writeMetadataType(method.getReturnType(), writer);

					IList<VariableElement> parameters = method.getParameters();
					if (!parameters.isEmpty())
					{
						writer.append(", \"paramTypes\": [");
						boolean firstParam = true;
						for (int j = 0, jLength = parameters.size(); j < jLength; j++)
						{
							VariableElement param = parameters.get(j);
							firstParam = writeStringIfFalse(", ", firstParam);
							VarSymbol var = (VarSymbol) param;
							String paramType = var.type.toString();
							writer.append('"');
							writeType(paramType);
							writer.append('"');
							paramNames[j] = paramType;
						}
						writer.append(']');
					}

					if (!singleMethod)
					{
						writer.append(", \"paramNames\": [");
						boolean firstParam = true;
						for (int j = 0, jLength = parameters.size(); j < jLength; j++)
						{
							VariableElement param = parameters.get(j);
							firstParam = writeStringIfFalse(", ", firstParam);
							VarSymbol var = (VarSymbol) param;
							String paramName = var.name.toString();
							writer.append('"').append(paramName).append('"');
							paramNames[j] = paramName;
						}
						writer.append(']');

						if (!ambiguousParameterNames)
						{
							Arrays.sort(paramNames);
							Method existing = paramNamesMaps.put(Arrays.deepToString(paramNames), method);
							if (existing != null)
							{
								ambiguousParameterNames = true;
								toDoWriter.write("Ambiguous parameter names", method);
							}
						}
					}
					writer.append(" }");
				}
				context.decrementIndentationLevel();
				newLineIndentIfFalse(singleMethod);
				writer.append(']');
			}
		}
		context.decrementIndentationLevel();
		newLineIndent();
		writer.append("]}");
	}

	protected ArrayList<Method>[] bucketSortMethods(ArrayList<Method> methods)
	{
		int maxParams = 0;
		for (Method method : methods)
		{
			int size = method.getParameters().size();
			if (size > maxParams)
			{
				maxParams = size;
			}
		}

		@SuppressWarnings("unchecked")
		ArrayList<Method>[] methodBuckets = new ArrayList[maxParams + 1];
		for (Method method : methods)
		{
			int size = method.getParameters().size();
			ArrayList<Method> bucket = methodBuckets[size];
			if (bucket == null)
			{
				bucket = new ArrayList<Method>();
				methodBuckets[size] = bucket;
			}
			bucket.add(method);
		}

		return methodBuckets;
	}

	protected void writeMetadataType(String type, IWriter writer)
	{
		writer.append("\"valueType\": \"");
		writeType(type);
		writer.append('"');
	}

	@Override
	public void writeAnnotations(BaseJavaClassModel model)
	{
		IList<Annotation> annotations = filterAnnotations(model.getAnnotations());
		if (annotations.isEmpty())
		{
			return;
		}

		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append(", \"at\": [");
		for (int i = 0, size = annotations.size(); i < size; i++)
		{
			Annotation annotation = annotations.get(i);
			writeAnnotation(annotation);
		}
		writer.append(']');
	}

	protected ArrayList<Annotation> filterAnnotations(IList<Annotation> annotations)
	{
		ArrayList<Annotation> filtered = new ArrayList<>(1);
		for (int i = 0, size = annotations.size(); i < size; i++)
		{
			Annotation annotation = annotations.get(i);
			if (Autowired.class.getName().equals(annotation.getType()) || //
					LogInstance.class.getName().equals(annotation.getType()) || //
					Property.class.getName().equals(annotation.getType()))
			{
				filtered.add(annotation);
			}
		}
		return filtered;
	}

	@Override
	public void writeAnnotation(Annotation annotation)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		IMap<String, AnnotationValue> properties = annotation.getProperties();
		AnnotationValue value = properties.get("value");
		if (properties.size() == 0)
		{
			writer.append('"');
			writeType(annotation.getType());
			writer.append('"');
			return;
		}
		else if (properties.size() == 1 && value != null)
		{
			writer.append("{\"");
			writeType(annotation.getType());
			writer.append("\": \"").append(value.getValue().toString());
			writer.append("\"}");
		}
		else
		{
			writer.append("{\"");
			writeType(annotation.getType());
			writer.append("\": {");
			boolean firstProperty = true;
			for (Entry<String, AnnotationValue> entry : properties)
			{
				firstProperty = writeStringIfFalse(", ", firstProperty);
				String propertyName = entry.getKey();
				writer.append('"').append(propertyName).append("\": ").append(entry.getValue().toString());
			}
			writer.append("}}");
		}
	}

	@Override
	public void writeGenericTypeArguments(List<Type> genericTypeArguments)
	{
	}

	@Override
	public void writeMethodArguments(List<JCExpression> methodArguments)
	{
	}

	@Override
	public void writeMethodArguments(JCExpression methodInvocation)
	{
	}

	@Override
	public void writeVariableName(String varName)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		// TODO
		// if (!getLanguageSpecific().getMethodScopeVars().contains(varName))
		// {
		// writer.append("this.");
		// }

		varName = convertVariableName(varName);

		writer.append(varName);
	}

	@Override
	public String convertVariableName(String varName)
	{
		if (RESERVED_WORDS.contains(varName))
		{
			varName += "_";
		}
		return varName;
	}

	@Override
	public void writeExpressionTree(Tree expression)
	{
		if (expression == null)
		{
			return;
		}

		Kind kind = expression.getKind();
		IExpressionHandler expressionHandler = expressionHandlerRegistry.getExtension(Lang.JS + kind);
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
		IStatementHandlerExtension<Tree> stmtHandler = statementHandlerRegistry.getExtension(Lang.JS + kind);
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

	@Override
	public String convertType(String typeName, boolean direct)
	{
		ParamChecker.assertParamNotNullOrEmpty(typeName, "typeName");

		typeName = typeName.trim();
		String[] mappedTypeName = javaTypeToJsMap.get(typeName);
		if (mappedTypeName == null)
		{
			if (typeName.endsWith("[]"))
			{
				String convertedType = convertType(typeName.substring(0, typeName.length() - 2), direct);
				convertedType += "[]";
				return convertedType;
			}
			Matcher genericTypeMatcher = ASTHelper.genericTypePattern.matcher(typeName);
			if (genericTypeMatcher.matches())
			{
				String plainType = genericTypeMatcher.group(1);
				String convertedType = convertType(plainType, direct);
				return convertedType;
			}

			if (!direct)
			{
				typeName = astHelper.resolveFqTypeFromTypeName(typeName);
				genericTypeMatcher = ASTHelper.genericTypePattern.matcher(typeName);
				if (genericTypeMatcher.matches())
				{
					typeName = genericTypeMatcher.group(1);
				}
				typeName = prefixModification(typeName, context);
				mappedTypeName = new String[] { StringConversionHelper.upperCaseFirst(objectCollector, typeName) };
			}
			else
			{
				mappedTypeName = new String[] { typeName };
			}
		}
		ISet<TypeUsing> usedTypes = context.getUsedTypes();
		if (usedTypes != null)
		{
			usedTypes.add(new TypeUsing(createNamespace(mappedTypeName[0]), false));
		}

		String convertedType = mappedTypeName[0];
		if (GENERIC_NAME.matcher(convertedType).matches())
		{
			convertedType = OBJECT;
		}

		return convertedType;
	}

	protected void writeTypeIntern(String typeName, boolean direct)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String convertedType = convertType(typeName, direct);
		writer.append(convertedType);
	}

	protected String prefixModification(String name, IConversionContext context)
	{
		String nsPrefixRemove = context.getNsPrefixRemove();
		if (name.startsWith(nsPrefixRemove))
		{
			int removeLength = nsPrefixRemove.length();
			name = name.substring(removeLength);
		}

		String nsPrefixAdd = context.getNsPrefixAdd();
		if (nsPrefixAdd != null)
		{
			name = nsPrefixAdd + name;
		}
		return name;
	}

	@Override
	public String createOverloadedMethodNamePostfix(IList<VariableElement> parameters)
	{
		if (parameters.isEmpty())
		{
			return "_none";
		}
		else
		{
			int length = parameters.size();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < length; i++)
			{
				VariableElement param = parameters.get(i);
				VarSymbol var = (VarSymbol) param;
				String paramTypeName = var.type.toString();
				paramTypeName = removeGenerics(paramTypeName);
				paramTypeName = paramTypeName.replaceAll("\\.", "_");
				paramTypeName = StringConversionHelper.underscoreToCamelCase(objectCollector, paramTypeName);
				paramTypeName = StringConversionHelper.upperCaseFirst(objectCollector, paramTypeName);
				while (paramTypeName.endsWith("[]"))
				{
					paramTypeName = "ArrayOf" + paramTypeName.substring(0, paramTypeName.length() - 2);
				}
				sb.append('_').append(paramTypeName);
			}
			return sb.toString();
		}
	}

	@Override
	public String removeGenerics(String name)
	{
		return name.replaceAll("<.*>", "");
	}

	@Override
	public JavaClassInfo findClassInHierarchy(String className, JavaClassInfo current, IConversionContext context)
	{
		IMap<String, JavaClassInfo> fqNameToClassInfoMap = context.getFqNameToClassInfoMap();
		String genericsFreeName;
		while (current != null)
		{
			genericsFreeName = removeGenerics(current.getFqName());
			if (genericsFreeName.equals(className))
			{
				return current;
			}

			current = getSuperClassInfo(current, fqNameToClassInfoMap);
		}
		return null;
	}

	@Override
	public Field findFieldInHierarchy(String fieldName, JavaClassInfo current, IConversionContext context)
	{
		IMap<String, JavaClassInfo> fqNameToClassInfoMap = context.getFqNameToClassInfoMap();
		while (current != null)
		{
			Field field = current.getField(fieldName, true);
			if (field != null)
			{
				return field;
			}

			current = getSuperClassInfo(current, fqNameToClassInfoMap);
		}
		return null;
	}

	protected JavaClassInfo getSuperClassInfo(JavaClassInfo current, IMap<String, JavaClassInfo> fqNameToClassInfoMap)
	{
		String nameOfSuperClass = current.getNameOfSuperClass();
		if (nameOfSuperClass == null)
		{
			return null;
		}
		current = fqNameToClassInfoMap.get(nameOfSuperClass);
		current = current != null ? current : fqNameToClassInfoMap.get(removeGenerics(nameOfSuperClass));
		return current;
	}
}
