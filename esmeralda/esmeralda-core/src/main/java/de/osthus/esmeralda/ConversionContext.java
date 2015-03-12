package de.osthus.esmeralda;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.sun.source.tree.Tree;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ConversionContext implements IConversionContext
{
	protected static final Pattern extendsFromPattern = Pattern.compile("\\s*\\?\\s+extends\\s+(.+)\\s*");

	protected static final Pattern variableName = Pattern.compile("[a-z]\\w*");

	protected static final HashSet<String> primitiveTypeNames = new HashSet<>(Arrays.asList("void", "boolean", "char", "byte", "short", "int", "long", "float",
			"double"));

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private ILanguageSpecific languageSpecific;

	private String language;

	private File targetPath;

	private String languagePath;

	private File snippetPath;

	private String pathPrefixRemove;

	private String nsPrefixAdd;

	private String nsPrefixRemove;

	private int indentationLevel;

	private JavaClassInfo classInfo;

	private HashSet<TypeUsing> usedTypes;

	private IMap<String, String> imports;

	private IList<TypeUsing> usings;

	private Field field;

	private Method method;

	private IWriter writer;

	private IClassInfoManager classInfoManager;

	private ILanguageHelper languageHelper;

	private ISnippetManager snippetManager;

	protected HashMap<String, Integer> calledMethods;

	protected HashSet<String> definedMethods;

	private final ArrayList<IPostProcess> postProcesses = new ArrayList<IPostProcess>();

	private boolean dryRun = false;

	private StatementCount metric;

	private boolean isGenericTypeSupported;

	private boolean skipFirstBlockStatement;

	private final ArrayList<HashMap<String, String>> sourceToTargetSymbolMapStack = new ArrayList<HashMap<String, String>>();

	private final ArrayList<ArrayList<HashMap<String, JavaClassInfo>>> variableDeclsOnStack = new ArrayList<ArrayList<HashMap<String, JavaClassInfo>>>();

	private final ArrayList<String> typeOnStack = new ArrayList<String>();

	private Tree currentTree;

	private final HashMap<String, JavaClassInfo> typeErasureHints = new HashMap<String, JavaClassInfo>();

	public ConversionContext()
	{
		startWriteToStash();
	}

	@Override
	public IConversionContext getCurrent()
	{
		return this;
	}

	@Override
	public ILanguageSpecific getLanguageSpecific()
	{
		return languageSpecific;
	}

	public void setLanguageSpecific(ILanguageSpecific languageSpecific)
	{
		this.languageSpecific = languageSpecific;
	}

	public void setClassInfoManager(IClassInfoManager classInfoManager)
	{
		this.classInfoManager = classInfoManager;
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
	public String getPathPrefixRemove()
	{
		return pathPrefixRemove;
	}

	public void setPathPrefixRemove(String pathPrefixRemove)
	{
		this.pathPrefixRemove = pathPrefixRemove;
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
		setMethodIntern(method);
		if (method != null)
		{
			addDefinedMethod(classInfo, method);
		}
	}

	@Override
	public void setMethodIntern(Method method)
	{
		this.method = method;
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

	public HashMap<String, Integer> getCalledMethods()
	{
		return calledMethods;
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

	public HashSet<String> getDefinedMethods()
	{
		return definedMethods;
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
		variableDeclsOnStack.add(new ArrayList<HashMap<String, JavaClassInfo>>());
		pushVariableDeclBlock();
	}

	@Override
	public void endWriteToStash()
	{
		sourceToTargetSymbolMapStack.popLastElement();
		typeOnStack.popLastElement();
		variableDeclsOnStack.popLastElement();
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

	@Override
	public Object pushTypeErasureHint(JavaClassInfo classInfo)
	{
		if (!classInfo.isArray())
		{
			if (typeErasureHints.putIfNotExists(classInfo.getNonGenericFqName(), classInfo))
			{
				return classInfo;
			}
			return null;
		}
		ArrayList<JavaClassInfo> pushedClassInfos = new ArrayList<JavaClassInfo>();
		JavaClassInfo currClassInfo = classInfo;
		while (currClassInfo.isArray())
		{
			if (typeErasureHints.putIfNotExists(classInfo.getNonGenericFqName(), classInfo))
			{
				pushedClassInfos.add(currClassInfo);
			}
			currClassInfo = currClassInfo.getComponentType();
		}
		if (typeErasureHints.putIfNotExists(currClassInfo.getNonGenericFqName(), currClassInfo))
		{
			pushedClassInfos.add(currClassInfo);
		}
		if (pushedClassInfos.size() > 1)
		{
			return pushedClassInfos;
		}
		else if (pushedClassInfos.size() == 1)
		{
			return pushedClassInfos.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void popTypeErasureHint(Object pushResult)
	{
		if (pushResult == null)
		{
			return;
		}
		if (pushResult instanceof ArrayList)
		{
			ArrayList<JavaClassInfo> pushedClassInfos = (ArrayList<JavaClassInfo>) pushResult;
			for (int a = pushedClassInfos.size(); a-- > 0;)
			{
				typeErasureHints.remove(pushedClassInfos.get(a).getNonGenericFqName());
			}
		}
		else
		{
			typeErasureHints.remove(((JavaClassInfo) pushResult).getNonGenericFqName());
		}
	}

	@Override
	public JavaClassInfo lookupTypeErasureHint(String fqName)
	{
		return typeErasureHints.get(fqName);
	}

	@Override
	public void pushVariableDecl(String variableName, JavaClassInfo type)
	{
		ArrayList<HashMap<String, JavaClassInfo>> variableDeclBlocks = variableDeclsOnStack.peek();
		HashMap<String, JavaClassInfo> variableDecls = variableDeclBlocks.peek();
		if (variableDecls == null)
		{
			throw new IllegalStateException("No variable block on the stack to add declaration to");
		}
		if (!variableDecls.putIfNotExists(variableName, type))
		{
			throw new IllegalStateException("Duplicate variable declaration within block: " + type.getFqName() + " " + variableName);
		}
	}

	@Override
	public JavaClassInfo lookupVariableDecl(String variableName)
	{
		ArrayList<ArrayList<HashMap<String, JavaClassInfo>>> variableDeclsOnStack = this.variableDeclsOnStack;
		for (int a = variableDeclsOnStack.size(); a-- > 0;)
		{
			ArrayList<HashMap<String, JavaClassInfo>> variableDeclBlocks = variableDeclsOnStack.get(a);
			if (variableDeclBlocks.size() == 0)
			{
				return null;
			}
			for (int b = variableDeclBlocks.size(); b-- > 0;)
			{
				HashMap<String, JavaClassInfo> variableDecls = variableDeclBlocks.get(b);
				JavaClassInfo variableType = variableDecls.get(variableName);
				if (variableType != null)
				{
					return variableType;
				}
			}
		}
		return null;
	}

	@Override
	public void pushVariableDeclBlock()
	{
		ArrayList<HashMap<String, JavaClassInfo>> variableDeclBlocks = variableDeclsOnStack.peek();
		variableDeclBlocks.add(new HashMap<String, JavaClassInfo>());
	}

	@Override
	public void popVariableDeclBlock()
	{
		ArrayList<HashMap<String, JavaClassInfo>> variableDeclBlocks = variableDeclsOnStack.peek();
		variableDeclBlocks.popLastElement();
	}
}
