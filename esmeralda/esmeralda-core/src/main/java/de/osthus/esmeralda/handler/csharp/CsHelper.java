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
import de.osthus.esmeralda.CodeVisitor;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.TypeUsing;
import de.osthus.esmeralda.handler.ASTHelper;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IExpressionHandler;
import de.osthus.esmeralda.handler.IExpressionHandlerRegistry;
import de.osthus.esmeralda.handler.IStatementHandlerExtension;
import de.osthus.esmeralda.handler.IStatementHandlerRegistry;
import de.osthus.esmeralda.handler.IUsedVariableDelegate;
import de.osthus.esmeralda.handler.IVariable;
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

	protected static final HashMap<String, String> javaTypeToCsharpRenameMap = new HashMap<String, String>();

	protected static final HashMap<String, String> implicitJavaImportsMap = new HashMap<String, String>();

	protected static final HashMap<String, String> annotationTargetMap = new HashMap<String, String>();

	protected static final Pattern dotSplit = Pattern.compile(Pattern.quote("."));

	protected static final Pattern upperCaseStart = Pattern.compile("^[A-Z].*");

	static
	{
		mapReplace("void", "void");
		mapReplace("boolean", "bool");
		mapReplace("char", "char");
		mapReplace("byte", "sbyte");
		mapReplace("short", "short");
		mapReplace("int", "int");
		mapReplace("long", "long");
		mapReplace("float", "float");
		mapReplace("double", "double");
		mapReplace(java.lang.Void.class.getName(), "void");
		mapReplace(java.lang.Boolean.class.getName(), "bool?");
		mapReplace(java.lang.Character.class.getName(), "char?");
		mapReplace(java.lang.Byte.class.getName(), "sbyte?");
		mapReplace(java.lang.Short.class.getName(), "short?");
		mapReplace(java.lang.Integer.class.getName(), "int?");
		mapReplace(java.lang.Long.class.getName(), "long?");
		mapReplace(java.lang.Float.class.getName(), "float?");
		mapReplace(java.lang.Double.class.getName(), "double?");
		mapReplace(java.lang.String.class.getName(), "System.String");
		mapReplace(java.lang.annotation.Annotation.class.getName(), "System.Attribute");
		mapReplace(java.lang.annotation.Target.class.getName(), "System.AttributeUsageAttribute");
		mapReplace(java.lang.Class.class.getName(), "System.Type");
		mapReplace(java.lang.Class.class.getName() + "<?>", "System.Type");
		mapReplace(java.lang.Exception.class.getName(), "System.Exception");
		mapReplace(java.lang.IllegalArgumentException.class.getName(), "System.ArgumentException");
		mapReplace(java.lang.IllegalStateException.class.getName(), "System.Exception");
		mapReplace(java.lang.Iterable.class.getName(), "System.Collections.Generic.IEnumerable");
		mapReplace(java.lang.Throwable.class.getName(), "System.Exception");
		mapReplace(java.lang.Object.class.getName(), "System.Object");
		mapReplace(java.lang.StringBuilder.class.getName(), "System.Text.StringBuilder");
		mapReplace(java.lang.reflect.Constructor.class.getName(), "System.Reflection.ConstructorInfo");
		mapReplace(java.lang.reflect.Field.class.getName(), "System.Reflection.FieldInfo");
		mapReplace(java.lang.reflect.Method.class.getName(), "System.Reflection.MethodInfo");
		mapReplace(java.lang.ref.Reference.class.getName(), "System.WeakReference");
		mapReplace(java.lang.ref.SoftReference.class.getName(), "System.WeakReference");
		mapReplace(java.lang.ref.WeakReference.class.getName(), "System.WeakReference");
		mapReplace(java.lang.RuntimeException.class.getName(), "System.Exception");
		mapReplace(java.lang.StackTraceElement.class.getName(), "System.Diagnostics.StackFrame");
		mapReplace(java.lang.ThreadLocal.class.getName(), "System.Threading.ThreadLocal", "De.Osthus.Ambeth.Util.ThreadLocal");
		mapReplace(java.io.InputStream.class.getName(), "System.IO.Stream");
		mapReplace(java.io.OutputStream.class.getName(), "System.IO.Stream");
		mapReplace(java.util.ArrayList.class.getName(), "System.Collections.Generic.List");
		mapReplace(java.util.Collection.class.getName(), "System.Collections.Generic.ICollection");
		mapReplace(java.util.Enumeration.class.getName(), "System.Collections.Generic.IEnumerator");
		mapReplace(java.util.Iterator.class.getName(), "System.Collections.Generic.IEnumerator");
		mapReplace(java.util.List.class.getName(), "System.Collections.Generic.IList");
		mapReplace(java.util.Map.class.getName(), "De.Osthus.Ambeth.Collections.IMap");
		mapReplace(java.util.Map.class.getName() + ".Entry", "De.Osthus.Ambeth.Collections.Entry"); // Inner classes are appended with '$'
		mapReplace(java.util.regex.Pattern.class.getName(), "System.Text.RegularExpressions.Regex");
		mapReplace(java.util.concurrent.locks.Lock.class.getName(), "De.Osthus.Ambeth.Util.Lock");
		mapReplace(de.osthus.ambeth.collections.ArrayList.class.getName(), "System.Collections.Generic.List");
		mapReplace(de.osthus.ambeth.collections.IList.class.getName(), "System.Collections.Generic.IList");
		mapRename(de.osthus.ambeth.collections.HashSet.class.getName(), "De.Osthus.Ambeth.Collections.CHashSet");

		// mapRename(de.osthus.ambeth.collections.IList.class.getName(), "De.Osthus.Ambeth.Collections.IIList");

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

	protected static final void mapReplace(String key, String... values)
	{
		javaTypeToCsharpMap.put(key, values);
	}

	protected static final void mapRename(String key, String value)
	{
		javaTypeToCsharpRenameMap.put(key, value);
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
	public CsSpecific getLanguageSpecific()
	{
		IConversionContext context = this.context.getCurrent();
		CsSpecific csSpecific = (CsSpecific) context.getLanguageSpecific();
		return csSpecific;
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

	// TODO Move the next to method (and those from the JsHelper) to new class (?EsmeraldaHelper?)
	@Override
	public void forAllUsedVariables(IUsedVariableDelegate usedVariableDelegate)
	{
		IConversionContext context = this.context.getCurrent();
		JavaClassInfo classInfo = context.getClassInfo();

		forAllUsedVariables(classInfo, usedVariableDelegate);
	}

	@Override
	public void forAllUsedVariables(JavaClassInfo classInfo, IUsedVariableDelegate usedVariableDelegate)
	{
		IConversionContext context = this.context.getCurrent();
		ILanguageHelper languageHelper = context.getLanguageHelper();
		IWriter writer = context.getWriter();
		IList<IVariable> allUsedVariables = classInfo.getAllUsedVariables();

		boolean firstVariable = true;
		HashSet<String> alreadyHandled = new HashSet<>();
		for (IVariable usedVariable : allUsedVariables)
		{
			String name = usedVariable.getName();
			if (!alreadyHandled.add(name))
			{
				// The IVariable instances have no equals(). So there are duplicates.
				continue;
			}

			usedVariableDelegate.invoke(usedVariable, firstVariable, context, languageHelper, writer);
			firstVariable = false;
		}
	}

	@Override
	public void writeAsTypeOf(String typeName)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		writer.append("typeof(");
		writeTypeIntern(typeName, false);
		writer.append(')');
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
			String[] typeAndGeneric = astHelper.parseGenericType(typeName);
			if (typeAndGeneric.length == 2)
			{
				String plainType = typeAndGeneric[0];

				writeTypeIntern(plainType, direct);
				if (Class.class.getName().equals(plainType))
				{
					// in C# the type handle is not generic so we intentionally "lose" the generic type information here
					return;
				}

				writer.append('<');
				String typeArguments = typeAndGeneric[1];
				typeArguments = ASTHelper.genericTypeExtendsPattern.matcher(typeArguments).replaceAll("");
				String[] typeArgumentsSplit = astHelper.splitTypeArgument(typeArguments);
				boolean firstArgument = true;
				for (String typeArgumentSplit : typeArgumentsSplit)
				{
					firstArgument = writeStringIfFalse(",", firstArgument);
					if ("?".equals(typeArgumentSplit))
					{
						writeTypeIntern("Object", true);
					}
					else
					{
						writeTypeIntern(typeArgumentSplit, direct);
					}
				}
				writer.append('>');
				return;
			}
			String renamedTypeName = getValueFromFqNameMap(javaTypeToCsharpRenameMap, typeName);
			if (renamedTypeName != null)
			{
				typeName = renamedTypeName;
			}
			if (!direct)
			{
				typeName = astHelper.resolveFqTypeFromTypeName(typeName);
				typeName = createFqName(typeName);
				mappedTypeName = new String[] { typeName };
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
		String csharpName = javaTypeToCsharpRenameMap.get(mappedTypeName[0]);
		if (csharpName == null)
		{
			csharpName = mappedTypeName[0];
		}
		writer.append(removeDollars(csharpName));
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

	protected <V> V getValueFromFqNameMap(IMap<String, V> map, String fqName)
	{
		V value = map.get(fqName);
		if (value != null)
		{
			return value;
		}
		String[] parsedGenericType = astHelper.parseGenericType(fqName);
		return map.get(parsedGenericType[0]);
	}

	@Override
	public void writeSimpleName(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String[] csharpNames = getValueFromFqNameMap(javaTypeToCsharpMap, classInfo.getFqName());
		String csharpName = null;
		if (csharpNames != null)
		{
			csharpName = csharpNames[0];
		}
		if (csharpName == null)
		{
			csharpName = getValueFromFqNameMap(javaTypeToCsharpRenameMap, classInfo.getFqName());
		}
		if (csharpName == null)
		{
			String simpleName = classInfo.getName();
			simpleName = removeDollars(simpleName);
			writer.append(simpleName);
			if (classInfo.getNonGenericName().equals(simpleName))
			{
				// check if there are really no generic type arguments
				writeGenericTypeArguments(classInfo.getTypeArguments());
			}
			return;
		}
		String[] csharpGenericName = astHelper.parseGenericType(csharpName);
		if (csharpGenericName.length == 1)
		{
			String[] javaGenericName = astHelper.parseGenericType(classInfo.getFqName());
			if (javaGenericName.length == 2)
			{
				// append generic info to the csharp type
				csharpName += "<" + javaGenericName[1] + ">";
			}
		}
		Matcher matcher = CodeVisitor.fqPattern.matcher(csharpName);
		if (!matcher.matches())
		{
			throw new IllegalStateException("Not a full qualified type: " + csharpName);
		}
		String simpleName = matcher.group(2);
		simpleName = removeDollars(simpleName);
		writer.append(simpleName);
	}

	protected String resolveSimpleNonGenericName(JavaClassInfo classInfo, boolean noDollars)
	{
		String[] csharpNames = getValueFromFqNameMap(javaTypeToCsharpMap, classInfo.getFqName());
		String csharpName = null;
		if (csharpNames != null)
		{
			csharpName = csharpNames[0];
		}
		if (csharpName == null)
		{
			csharpName = getValueFromFqNameMap(javaTypeToCsharpRenameMap, classInfo.getFqName());
		}
		if (csharpName == null)
		{
			String className = classInfo.getNonGenericName();
			if (noDollars)
			{
				className = removeDollars(className);
			}
			return className;
		}
		String[] parsedGenericType = astHelper.parseGenericType(csharpName);
		Matcher matcher = CodeVisitor.fqPattern.matcher(parsedGenericType[0]);
		if (!matcher.matches())
		{
			throw new IllegalStateException("Not a full qualified type: " + parsedGenericType[0]);
		}
		String simpleNonGenericName = matcher.group(2);
		if (noDollars)
		{
			simpleNonGenericName = removeDollars(simpleNonGenericName);
		}
		return simpleNonGenericName;
	}

	@Override
	public void writeSimpleNonGenericName(JavaClassInfo classInfo)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();

		String simpleNonGenericName = resolveSimpleNonGenericName(classInfo, true);
		writer.append(simpleNonGenericName);
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

		String pathPrefixRemove = context.getPathPrefixRemove();
		if (pathPrefixRemove != null && packageName.toLowerCase().startsWith(pathPrefixRemove.toLowerCase()))
		{
			int removeLength = pathPrefixRemove.length();
			packageName = packageName.substring(removeLength);
		}
		// packageName = packageName.toLowerCase();

		String[] packageSplit = dotSplit.split(packageName);
		checkForInnerClassPackage(packageSplit);
		packageSplit = camelCaseName(packageSplit);
		String relativeTargetPathName = StringConversionHelper.implode(objectCollector, packageSplit, File.separator);

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
		String nonGenericName = resolveSimpleNonGenericName(classInfo, false);
		return nonGenericName + ".cs";
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

	private String createFqName(String fqTypeName)
	{
		Pattern fqNamePattern = Pattern.compile("((.+)\\.)([^\\.]+?)");
		Matcher matcher = fqNamePattern.matcher(fqTypeName);
		if (matcher.matches())
		{
			String packageName = matcher.group(2);
			String simpleName = matcher.group(3);
			packageName = createNamespace(packageName);
			fqTypeName = packageName + "." + simpleName;
		}
		return fqTypeName;
	}

	@Override
	public String createNamespace(String packageName)
	{
		IConversionContext context = this.context.getCurrent();

		String nsPrefixRemove = context.getNsPrefixRemove();
		if (nsPrefixRemove != null && packageName.toLowerCase().startsWith(nsPrefixRemove.toLowerCase()))
		{
			int removeLength = nsPrefixRemove.length();
			packageName = packageName.substring(removeLength);
		}
		// packageName = packageName.toLowerCase();

		String nsPrefixAdd = context.getNsPrefixAdd();
		if (nsPrefixAdd != null)
		{
			packageName = nsPrefixAdd + packageName;
		}

		String[] packageSplit = dotSplit.split(packageName);
		checkForInnerClassPackage(packageSplit);
		checkForWordException(packageSplit);
		packageSplit = camelCaseName(packageSplit);
		String namespace = StringConversionHelper.implode(objectCollector, packageSplit, ".");

		return namespace;
	}

	/**
	 * Prevents a collision between a class name and a package name.
	 * 
	 * @param packageSplit
	 *            Parts of the package name
	 */
	protected void checkForInnerClassPackage(String[] packageSplit)
	{
		for (int i = packageSplit.length; i-- > 0;)
		{
			String part = packageSplit[i];
			if (!upperCaseStart.matcher(part).matches())
			{
				break;
			}

			packageSplit[i] = part + "NS";
		}
	}

	/**
	 * Prevents a collision between the Exception class and a package name.
	 * 
	 * @param packageSplit
	 */
	private void checkForWordException(String[] packageSplit)
	{
		for (int i = 0, length = packageSplit.length; i < length; i++)
		{
			String part = packageSplit[i];
			if ("exception".equalsIgnoreCase(part))
			{
				packageSplit[i] = part + "s";
			}
		}
	}

	protected String[] camelCaseName(String[] strings)
	{
		String[] camelCase = new String[strings.length];
		for (int a = strings.length; a-- > 0;)
		{
			camelCase[a] = StringConversionHelper.upperCaseFirst(objectCollector, strings[a]);
		}
		return camelCase;
		// return strings;
	}

	@Override
	public String createMethodName(String methodName)
	{
		methodName = methodName.split("<", 2)[0]; // Generics are written elsewhere
		methodName = removeDollars(methodName);
		methodName = StringConversionHelper.upperCaseFirst(objectCollector, methodName);
		return methodName;
	}

	protected String removeDollars(String name)
	{
		name = name.replaceAll("\\$", "_");
		return name;
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

	public void writeGenericTypeArguments(JavaClassInfo[] genericTypeArguments)
	{
		if (genericTypeArguments == null || genericTypeArguments.length == 0)
		{
			return;
		}
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		writer.append('<');
		for (int a = 0, size = genericTypeArguments.length; a < size; a++)
		{
			JavaClassInfo genericTypeArgument = genericTypeArguments[a];
			if (a > 0)
			{
				writer.append(", ");
			}
			writeType(genericTypeArgument.getFqName());
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
		writer.append(")]");
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
		boolean isStaticWritten = false;
		if (javaClassModel.isStatic())
		{
			boolean isEnum = false;
			boolean isInnerClass = false;
			if (javaClassModel instanceof JavaClassInfo)
			{
				// A class cannot be static and sealed at the same time. Enums are generated as sealed.
				// Also inner classes in Java are often declared static to be instantiatable independend from its owner - this has the opposite effect in C#.
				JavaClassInfo javaClassInfo = (JavaClassInfo) javaClassModel;
				isEnum = javaClassInfo.isEnum();
				isInnerClass = "MEMBER".equals(javaClassInfo.getNestingKind());
			}
			if (!isEnum && !isInnerClass)
			{
				firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
				writer.append("static");
				isStaticWritten = true;
			}
		}
		if (javaClassModel.isFinal())
		{
			if (javaClassModel instanceof JavaClassInfo)
			{
				// A class cannot be static and sealed at the same time
				if (!isStaticWritten)
				{
					firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
					writer.append("sealed");
				}
			}
			else
			{
				firstKeyWord = writeStringIfFalse(" ", firstKeyWord);
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

		varName = convertVariableName(varName);

		writer.append(varName);
	}

	@Override
	public void writeVariableNameAccess(String varName)
	{
		writeVariableName(varName);
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
		IConversionContext context = this.context.getCurrent();
		Tree previousTree = context.getCurrentTree();
		context.setCurrentTree(expression);
		try
		{
			Kind kind = expression.getKind();
			IExpressionHandler expressionHandler = expressionHandlerRegistry.getExtension(Lang.C_SHARP + kind);
			if (expressionHandler != null)
			{
				expressionHandler.handleExpression(expression);
				return;
			}
			handleChildStatement(expression);
		}
		finally
		{
			context.setCurrentTree(previousTree);
		}
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
