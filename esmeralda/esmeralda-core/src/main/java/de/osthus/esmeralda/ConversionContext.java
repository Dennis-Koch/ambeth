package de.osthus.esmeralda;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IClassInfoFactory;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ConversionContext implements IConversionContext
{
	protected static final Pattern extendsFromPattern = Pattern.compile("\\s*\\?\\s+extends\\s+(.+)\\s*");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private String language;

	private File targetPath;

	private String languagePath;

	private File snippetPath;

	private String nsPrefixAdd;

	private String nsPrefixRemove;

	private int indentationLevel;

	private JavaClassInfo classInfo;

	private IMap<String, JavaClassInfo> fqNameToClassInfoMap;

	private HashSet<TypeUsing> usedTypes;

	private IMap<String, String> imports;

	private IList<TypeUsing> usings;

	private Field field;

	private Method method;

	private IWriter writer;

	private IASTHelper astHelper;

	private ILanguageHelper languageHelper;

	private ISnippetManager snippetManager;

	protected HashMap<String, Integer> calledMethods;

	protected HashSet<String> definedMethods;

	private final ArrayList<IPostProcess> postProcesses = new ArrayList<IPostProcess>();

	private boolean dryRun = false;

	private StatementCount metric;

	protected IClassInfoFactory classInfoFactory;

	private boolean isGenericTypeSupported;

	private boolean skipFirstBlockStatement;

	private final ArrayList<HashMap<String, String>> sourceToTargetSymbolMapStack = new ArrayList<HashMap<String, String>>();

	private final HashMap<AlreadyTriedKey, JavaClassInfo> alreadyTriedAndFailedMap = new HashMap<AlreadyTriedKey, JavaClassInfo>();

	private final ArrayList<String> typeOnStack = new ArrayList<String>();

	private Tree currentTree;

	public ConversionContext()
	{
		startWriteToStash();
	}

	public void setClassInfoFactory(IClassInfoFactory classInfoFactory)
	{
		this.classInfoFactory = classInfoFactory;
	}

	public void setAstHelper(IASTHelper astHelper)
	{
		this.astHelper = astHelper;
	}

	@Override
	public IConversionContext getCurrent()
	{
		return this;
	}

	@Override
	public void setCurrent(IConversionContext current)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGenericTypeSupported()
	{
		return isGenericTypeSupported;
	}

	public void setGenericTypeSupported(boolean isGenericTypeSupported)
	{
		this.isGenericTypeSupported = isGenericTypeSupported;
	}

	@Override
	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	@Override
	public File getTargetPath()
	{
		return targetPath;
	}

	@Override
	public void setTargetPath(File targetPath)
	{
		this.targetPath = targetPath;
	}

	@Override
	public void setFqNameToClassInfoMap(IMap<String, JavaClassInfo> fqNameToClassInfoMap)
	{
		this.fqNameToClassInfoMap = fqNameToClassInfoMap;
	}

	@Override
	public IMap<String, JavaClassInfo> getFqNameToClassInfoMap()
	{
		return fqNameToClassInfoMap;
	}

	@Override
	public IWriter getWriter()
	{
		return writer;
	}

	@Override
	public void setWriter(IWriter writer)
	{
		this.writer = writer;
	}

	@Override
	public boolean isDryRun()
	{
		return dryRun;
	}

	@Override
	public void setDryRun(boolean dryRun)
	{
		this.dryRun = dryRun;
	}

	@Override
	public ILanguageHelper getLanguageHelper()
	{
		return languageHelper;
	}

	public void setLanguageHelper(ILanguageHelper languageHelper)
	{
		this.languageHelper = languageHelper;
	}

	protected String getResolveGenericFqTypeName(String fqTypeName)
	{
		// Method method = getMethod();
		// for (VariableElement parameter : method.getParameters())
		// {
		// {
		// TypeMirror asType = parameter.asType();
		// if (asType instanceof ArrayType)
		// {
		// ArrayType arrayType = (ArrayType) asType;
		// if (arrayType.elemtype instanceof TypeVar)
		// {
		// Type upperBound = arrayType.elemtype.getUpperBound();
		//
		// }
		// }
		// if (asType.toString().equals(fqTypeName))
		// {
		//
		// }
		// }
		return fqTypeName;
		// }
	}

	@Override
	public JavaClassInfo resolveClassInfo(String fqTypeName)
	{
		return resolveClassInfo(fqTypeName, false);
	}

	@Override
	public JavaClassInfo resolveClassInfo(String fqTypeName, boolean tryOnly)
	{
		return resolveClassInfo(fqTypeName, tryOnly, true);
	}

	protected JavaClassInfo classInfoResolved(String fqTypeName, JavaClassInfo resolvedClassInfo)
	{
		AlreadyTriedKey alreadyTriedKey = new AlreadyTriedKey(getClassInfo().getTreePath(), fqTypeName);
		alreadyTriedAndFailedMap.put(alreadyTriedKey, resolvedClassInfo);
		return resolvedClassInfo;
	}

	protected JavaClassInfo resolveClassInfo(String fqTypeName, boolean tryOnly, boolean cascadeSearch)
	{
		if (fqTypeName == null || fqTypeName.length() == 0 || "<none>".equals(fqTypeName))
		{
			return null;
		}
		Method method = getMethod();
		TreePath classTreePath = getClassInfo().getTreePath();
		TreePath keyTreePath = method != null ? method.getPath() : classTreePath;
		AlreadyTriedKey alreadyTriedKey = new AlreadyTriedKey(keyTreePath, fqTypeName);
		JavaClassInfo scopedClassInfo = alreadyTriedAndFailedMap.get(alreadyTriedKey);
		if (scopedClassInfo != null)
		{
			return scopedClassInfo;
		}
		if (alreadyTriedAndFailedMap.containsKey(alreadyTriedKey))
		{
			return null;
		}
		String originalFqTypeName = fqTypeName;
		String[] parsedGenericType = parseGenericType(fqTypeName);
		if (parsedGenericType.length == 2)
		{
			String nonGenericType = parsedGenericType[0];
			String genericTypeArguments = parsedGenericType[1];
			JavaClassInfo nonGenericClassInfo = resolveClassInfo(nonGenericType, tryOnly);
			if (nonGenericClassInfo == null)
			{
				return classInfoResolved(originalFqTypeName, nonGenericClassInfo);
			}
			return classInfoResolved(originalFqTypeName, makeGenericClassInfo(nonGenericClassInfo, genericTypeArguments));
		}
		if (method != null)
		{
			TreePath methodTreePath = method.getPath();
			for (JCTypeParameter typeParameter : ((JCMethodDecl) methodTreePath.getLeaf()).typarams)
			{
				String typeName = typeParameter.type.tsym.name.toString();
				if (typeName.equals(fqTypeName))
				{
					List<JCExpression> bounds = typeParameter.bounds;
					if (bounds.size() == 0)
					{
						// extends from java.lang.Object
						JavaClassInfo boundClassInfo = resolveClassInfo(Object.class.getName());
						JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(boundClassInfo, typeName);
						return classInfoResolved(fqTypeName, classInfo);
					}
					if (bounds.size() != 1)
					{
						throw new IllegalStateException(typeParameter.toString());
					}
					JCExpression bound = bounds.get(0);
					JavaClassInfo boundClassInfo;
					this.method = null;
					try
					{
						boundClassInfo = resolveClassInfo(bound.type.toString());
					}
					finally
					{
						this.method = method;
					}
					JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(boundClassInfo, typeName);
					return classInfoResolved(fqTypeName, classInfo);
				}
			}
		}
		for (JCTypeParameter typeParameter : ((JCClassDecl) classTreePath.getLeaf()).getTypeParameters())
		{
			String typeName = typeParameter.type.tsym.name.toString();
			if (typeName.equals(fqTypeName))
			{
				List<JCExpression> bounds = typeParameter.bounds;
				if (bounds.size() == 0)
				{
					// extends from java.lang.Object
					JavaClassInfo boundClassInfo = resolveClassInfo(Object.class.getName());
					JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(boundClassInfo, typeName);
					return classInfoResolved(fqTypeName, classInfo);
				}
				JCExpression bound = bounds.get(0);
				JavaClassInfo boundClassInfo;
				this.method = null;
				try
				{
					boundClassInfo = resolveClassInfo(bound.type.toString());
				}
				finally
				{
					this.method = method;
				}
				JavaClassInfo classInfo = makeGenericClassInfoExtendsFrom(boundClassInfo, typeName);
				return classInfoResolved(fqTypeName, classInfo);
			}
		}
		fqTypeName = NewClassExpressionHandler.getFqNameFromAnonymousName(fqTypeName);
		fqTypeName = getResolveGenericFqTypeName(fqTypeName);
		JavaClassInfo classInfo = fqNameToClassInfoMap.get(fqTypeName);
		if (classInfo != null)
		{
			return classInfoResolved(originalFqTypeName, classInfo);
		}
		boolean isSimpleName = (fqTypeName.indexOf('.') == -1);
		if (isSimpleName && cascadeSearch)
		{
			JavaClassInfo contextualClassInfo = getClassInfo();
			if (contextualClassInfo != null)
			{
				// if it is not a variable symbol is can be a simpleName of a class in our current package or in our import scope
				classInfo = resolveClassInfo(contextualClassInfo.getFqName() + "." + fqTypeName, true, false);
				if (classInfo != null)
				{
					return classInfo;
				}
				classInfo = resolveClassInfo(contextualClassInfo.getPackageName() + "." + fqTypeName, true, false);
				if (classInfo != null)
				{
					return classInfo;
				}
			}
		}
		String javaLangFqTypeName = "java.lang." + fqTypeName;
		classInfo = fqNameToClassInfoMap.get(javaLangFqTypeName);
		if (classInfo != null)
		{
			return classInfoResolved(originalFqTypeName, classInfo);
		}
		// TODO ClassLoader skip generation?
		// if (fqTypeName.equals(ClassLoader.class.getName()))
		// {
		// throw new SkipGenerationException();
		// }
		classInfo = classInfoFactory.createClassInfo(fqTypeName);
		if (classInfo == null && isSimpleName && cascadeSearch)
		{
			TreePath currTreePath = classTreePath;
			while (!(currTreePath.getLeaf() instanceof JCCompilationUnit))
			{
				currTreePath = currTreePath.getParentPath();
			}
			for (JCImport importItem : ((JCCompilationUnit) currTreePath.getLeaf()).getImports())
			{
				JCFieldAccess fa = (JCFieldAccess) importItem.getQualifiedIdentifier();
				String simpleNameOfImport = fa.getIdentifier().toString();
				if ("*".equals(simpleNameOfImport))
				{
					// try the basePackage with the simpleName
					classInfo = resolveClassInfo(fa.getExpression().toString() + "." + fqTypeName, true, false);
					if (classInfo != null)
					{
						return classInfoResolved(originalFqTypeName, classInfo);
					}
				}
				else if (fqTypeName.equals(simpleNameOfImport))
				{
					classInfo = resolveClassInfo(fa.toString(), true, false);
					if (classInfo != null)
					{
						return classInfoResolved(originalFqTypeName, classInfo);
					}
				}
			}
		}
		if (classInfo == null)
		{
			if (tryOnly)
			{
				return classInfoResolved(originalFqTypeName, null);
			}
			throw new TypeResolveException(fqTypeName);
		}
		if (!fqNameToClassInfoMap.putIfNotExists(fqTypeName, classInfo))
		{
			log.warn("Duplicate type registration: " + fqTypeName);
			classInfo = fqNameToClassInfoMap.get(fqTypeName);
		}
		fqNameToClassInfoMap.putIfNotExists(classInfo.getFqName(), classInfo); // may be duplicated intentionally
		return classInfoResolved(originalFqTypeName, classInfo);
	}

	protected String[] parseGenericType(String fqTypeName)
	{
		int genericBracketCounter = 0;
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
		String nonGenericType = fqTypeName.substring(0, lastBracketOpening) + fqTypeName.substring(lastBracketClosing + 1);
		String genericTypeArguments = fqTypeName.substring(lastBracketOpening + 1, lastBracketClosing);
		return new String[] { nonGenericType, genericTypeArguments };
	}

	protected JavaClassInfo makeGenericClassInfoExtendsFrom(JavaClassInfo extendsFromClassInfo, String symbolName)
	{
		JavaClassInfo classInfo = new JavaClassInfo(extendsFromClassInfo.context);
		classInfo.setName(symbolName);
		classInfo.setPackageName(null);
		classInfo.setExtendsFrom(extendsFromClassInfo);

		classInfo.setPrivateFlag(extendsFromClassInfo.isPrivate());
		classInfo.setProtectedFlag(extendsFromClassInfo.isProtected());
		classInfo.setPublicFlag(extendsFromClassInfo.isPublic());
		classInfo.setFinalFlag(extendsFromClassInfo.isFinal());
		classInfo.setNameOfSuperClass(null);

		return classInfo;
	}

	protected JavaClassInfo makeGenericClassInfo(JavaClassInfo classInfo, String genericTypeArguments)
	{
		JavaClassInfo superClassInfo = null;
		String nameOfSuperClass = classInfo.getNameOfSuperClass();
		if (nameOfSuperClass != null)
		{
			superClassInfo = resolveClassInfo(nameOfSuperClass);
		}
		String[] typeArgumentsSplit = astHelper.splitTypeArgument(genericTypeArguments);
		JavaClassInfo[] typeArgumentCIs = new JavaClassInfo[typeArgumentsSplit.length];
		StringBuilder sb = new StringBuilder();
		sb.append(astHelper.extractNonGenericType(classInfo.getName()));
		boolean first = true;
		for (int a = typeArgumentsSplit.length; a-- > 0;)
		{
			if (first)
			{
				sb.append('<');
				first = false;
			}
			else
			{
				sb.append(',');
			}
			String typeArgumentSplit = typeArgumentsSplit[a];

			JavaClassInfo typeArgumentCI = null;
			if ("?".equals(typeArgumentSplit))
			{
				typeArgumentCI = resolveClassInfo(Object.class.getName(), false, false);
			}
			if (typeArgumentCI == null)
			{
				Matcher extendsFromMatcher = extendsFromPattern.matcher(typeArgumentSplit);
				if (extendsFromMatcher.matches())
				{
					String extendsFromType = extendsFromMatcher.group(1);
					typeArgumentCI = resolveClassInfo(extendsFromType);
				}
			}
			if (typeArgumentCI == null)
			{
				typeArgumentCI = resolveClassInfo(typeArgumentSplit, true);
			}
			if (typeArgumentCI == null)
			{
				if (!typeArgumentSplit.contains("."))
				{
					sb.append(typeArgumentSplit);
					continue;
				}
			}
			if (typeArgumentCI == null)
			{
				throw new TypeResolveException(typeArgumentSplit);
			}
			typeArgumentCIs[a] = typeArgumentCI;
			sb.append(typeArgumentCI.getFqName());
		}
		if (!first)
		{
			sb.append('>');
		}
		JavaClassInfo genericClassInfo = new JavaClassInfo(classInfo.context);
		genericClassInfo.setName(sb.toString());
		genericClassInfo.setPackageName(classInfo.getPackageName());
		genericClassInfo.setTypeArguments(typeArgumentCIs);

		genericClassInfo.setPrivateFlag(classInfo.isPrivate());
		genericClassInfo.setProtectedFlag(classInfo.isProtected());
		genericClassInfo.setPublicFlag(classInfo.isPublic());
		genericClassInfo.setFinalFlag(classInfo.isFinal());
		genericClassInfo.setNameOfSuperClass(classInfo.getNameOfSuperClass());
		for (String nameOfInterface : classInfo.getNameOfInterfaces())
		{
			genericClassInfo.addNameOfInterface(nameOfInterface);
		}
		for (Annotation annotation : classInfo.getAnnotations())
		{
			genericClassInfo.addAnnotation(annotation);
		}
		for (Field field : classInfo.getFields())
		{
			genericClassInfo.addField(field);
		}
		for (Method method : classInfo.getMethods())
		{
			genericClassInfo.addMethod(method);
		}
		return genericClassInfo;
	}

	@Override
	public void setTargetFile(File targetPath)
	{
		this.targetPath = targetPath;
	}

	@Override
	public String getLanguagePath()
	{
		return languagePath;
	}

	@Override
	public void setLanguagePath(String languagePath)
	{
		this.languagePath = languagePath;
	}

	@Override
	public File getSnippetPath()
	{
		return snippetPath;
	}

	public void setSnippetPath(File snippetPath)
	{
		this.snippetPath = snippetPath;
	}

	@Override
	public String getNsPrefixAdd()
	{
		return nsPrefixAdd;
	}

	@Override
	public void setNsPrefixAdd(String nsPrefixAdd)
	{
		this.nsPrefixAdd = nsPrefixAdd;
	}

	@Override
	public String getNsPrefixRemove()
	{
		return nsPrefixRemove;
	}

	@Override
	public void setNsPrefixRemove(String nsPrefixRemove)
	{
		this.nsPrefixRemove = nsPrefixRemove;
	}

	@Override
	public int getIndentationLevel()
	{
		return indentationLevel;
	}

	@Override
	public void setIndentationLevel(int indentationLevel)
	{
		this.indentationLevel = indentationLevel;
	}

	@Override
	public int incrementIndentationLevel()
	{
		indentationLevel++;
		return indentationLevel;
	}

	@Override
	public int decrementIndentationLevel()
	{
		indentationLevel--;
		return indentationLevel;
	}

	@Override
	public JavaClassInfo getClassInfo()
	{
		return classInfo;
	}

	@Override
	public void setClassInfo(JavaClassInfo classInfo)
	{
		this.classInfo = classInfo;
	}

	@Override
	public HashSet<TypeUsing> getUsedTypes()
	{
		return usedTypes;
	}

	@Override
	public void setUsedTypes(HashSet<TypeUsing> usedTypes)
	{
		this.usedTypes = usedTypes;
	}

	@Override
	public IMap<String, String> getImports()
	{
		return imports;
	}

	@Override
	public void setImports(IMap<String, String> imports)
	{
		this.imports = imports;
	}

	@Override
	public IList<TypeUsing> getUsings()
	{
		return usings;
	}

	@Override
	public void setUsings(IList<TypeUsing> usings)
	{
		this.usings = usings;
	}

	@Override
	public Field getField()
	{
		return field;
	}

	@Override
	public void setField(Field field)
	{
		this.field = field;
	}

	@Override
	public Method getMethod()
	{
		return method;
	}

	@Override
	public void setMethod(Method method)
	{
		this.method = method;
		addDefinedMethod(classInfo, method);
	}

	@Override
	public ISnippetManager getSnippetManager()
	{
		return snippetManager;
	}

	@Override
	public void setSnippetManager(ISnippetManager snippetManager)
	{
		this.snippetManager = snippetManager;
	}

	public void setCalledMethods(HashMap<String, Integer> calledMethods)
	{
		this.calledMethods = calledMethods;
	}

	@Override
	public void addCalledMethod(ITransformedMethod method)
	{
		String packageAndClassName = method.getOwner();
		packageAndClassName = packageAndClassName.replaceAll("<.*>", "");
		int lastDot = packageAndClassName.lastIndexOf(".");
		if (lastDot != -1)
		{
			String packageName = packageAndClassName.substring(0, lastDot);
			packageName = languageHelper.createNamespace(packageName);

			String className = packageAndClassName.substring(lastDot + 1);

			packageAndClassName = packageName + '.' + className;
		}
		String methodName = method.getName();

		String fullMethodName = packageAndClassName + '.' + methodName;
		Integer count = calledMethods.get(fullMethodName);
		if (count == null)
		{
			count = Integer.valueOf(0);
		}
		count++;
		calledMethods.put(fullMethodName, count);
	}

	public void setDefinedMethods(HashSet<String> definedMethods)
	{
		this.definedMethods = definedMethods;
	}

	protected void addDefinedMethod(JavaClassInfo classInfo, Method method)
	{
		String namespace = languageHelper.createNamespace();
		String className = classInfo.getName();
		String methodName = languageHelper.createMethodName(method.getName());
		String fullMethodName = namespace + '.' + className + '.' + methodName;
		fullMethodName = fullMethodName.replaceAll("<.*>", "");
		definedMethods.add(fullMethodName);
	}

	@Override
	public void queuePostProcess(IPostProcess postProcess)
	{
		postProcesses.add(postProcess);
	}

	@Override
	public IList<IPostProcess> getPostProcesses()
	{
		return postProcesses;
	}

	@Override
	public String getTypeOnStack()
	{
		return typeOnStack.peek();
	}

	@Override
	public void setTypeOnStack(String typeOnStack)
	{
		if ("<nulltype>".equals(typeOnStack))
		{
			typeOnStack = null;
		}
		this.typeOnStack.set(this.typeOnStack.size() - 1, typeOnStack);
	}

	@Override
	public StatementCount getMetric()
	{
		return metric;
	}

	@Override
	public void setMetric(StatementCount metric)
	{
		this.metric = metric;
	}

	@Override
	public String toString()
	{
		if (classInfo != null)
		{
			return classInfo.toString();
		}
		return super.toString();
	}

	@Override
	public boolean isSkipFirstBlockStatement()
	{
		return skipFirstBlockStatement;
	}

	@Override
	public void setSkipFirstBlockStatement(boolean skipFirstBlockStatement)
	{
		this.skipFirstBlockStatement = skipFirstBlockStatement;
	}

	@Override
	public void startWriteToStash()
	{
		sourceToTargetSymbolMapStack.add(new HashMap<String, String>());
		typeOnStack.add(typeOnStack.peek());
	}

	@Override
	public void endWriteToStash()
	{
		sourceToTargetSymbolMapStack.popLastElement();
		typeOnStack.popLastElement();
	}

	@Override
	public void mapSymbolTransformation(String sourceSymbol, String targetSymbol)
	{
		HashMap<String, String> sourceToTargetSymbolMap = sourceToTargetSymbolMapStack.peek();
		if (!sourceToTargetSymbolMap.putIfNotExists(sourceSymbol, targetSymbol))
		{
			throw new IllegalStateException("Duplicate symbol definition on same stack level");
		}
	}

	@Override
	public String getTransformedSymbol(String sourceSymbol)
	{
		for (int a = sourceToTargetSymbolMapStack.size(); a-- > 0;)
		{
			HashMap<String, String> sourceToTargetSymbolMap = sourceToTargetSymbolMapStack.get(a);
			String targetSymbol = sourceToTargetSymbolMap.get(sourceSymbol);
			if (targetSymbol != null)
			{
				return targetSymbol;
			}
		}
		return sourceSymbol;
	}

	@Override
	public Tree getCurrentTree()
	{
		return currentTree;
	}

	@Override
	public void setCurrentTree(Tree currentTree)
	{
		this.currentTree = currentTree;
	}
}
