package de.osthus.esmeralda;

import java.io.File;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCImport;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IASTHelper;
import de.osthus.esmeralda.handler.IClassInfoFactory;
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

	private final ArrayList<IPostProcess> postProcesses = new ArrayList<IPostProcess>();

	private boolean dryRun = false;

	private String typeOnStack;

	private StatementCount metric;

	protected IClassInfoFactory classInfoFactory;

	private boolean isGenericTypeSupported;

	private boolean skipFirstBlockStatement;

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

	protected JavaClassInfo resolveClassInfo(String fqTypeName, boolean tryOnly, boolean cascadeSearch)
	{
		if ("<none>".equals(fqTypeName))
		{
			return null;
		}
		if ("?".equals(fqTypeName))
		{
			return resolveClassInfo(Object.class.getName(), false, false);
		}
		fqTypeName = NewClassExpressionHandler.getFqNameFromAnonymousName(fqTypeName);
		fqTypeName = getResolveGenericFqTypeName(fqTypeName);
		JavaClassInfo classInfo = fqNameToClassInfoMap.get(fqTypeName);
		if (classInfo != null)
		{
			return classInfo;
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
		classInfo = fqNameToClassInfoMap.get("java.lang." + fqTypeName);
		if (classInfo != null)
		{
			return classInfo;
		}
		if (fqTypeName.equals(ClassLoader.class.getName()))
		{
			throw new SkipGenerationException();
		}
		String[] parsedGenericType = parseGenericType(fqTypeName);
		if (parsedGenericType.length == 2)
		{
			String nonGenericType = parsedGenericType[0];
			String genericTypeArguments = parsedGenericType[1];
			JavaClassInfo nonGenericClassInfo = resolveClassInfo(nonGenericType, tryOnly);
			if (nonGenericClassInfo == null)
			{
				return null;
			}
			return makeGenericClassInfo(nonGenericClassInfo, genericTypeArguments);
		}
		classInfo = classInfoFactory.createClassInfo(fqTypeName);
		if (classInfo == null && isSimpleName && cascadeSearch)
		{
			TreePath treePath = getClassInfo().getTreePath();
			while (!(treePath.getLeaf() instanceof JCCompilationUnit))
			{
				treePath = treePath.getParentPath();
			}
			for (JCImport importItem : ((JCCompilationUnit) treePath.getLeaf()).getImports())
			{
				JCFieldAccess fa = (JCFieldAccess) importItem.getQualifiedIdentifier();
				String simpleNameOfImport = fa.getIdentifier().toString();
				if ("*".equals(simpleNameOfImport))
				{
					// try the basePackage with the simpleName
					classInfo = resolveClassInfo(fa.getExpression().toString() + "." + fqTypeName, true, false);
					if (classInfo != null)
					{
						return classInfo;
					}
				}
				else if (fqTypeName.equals(simpleNameOfImport))
				{
					classInfo = resolveClassInfo(fa.toString(), true, false);
					if (classInfo != null)
					{
						return classInfo;
					}
				}
			}
		}
		if (classInfo == null)
		{
			if (tryOnly)
			{
				return null;
			}
			throw new TypeResolveException(fqTypeName);
		}
		if (!fqNameToClassInfoMap.putIfNotExists(fqTypeName, classInfo))
		{
			log.warn("Duplicate type registration: " + fqTypeName);
			classInfo = fqNameToClassInfoMap.get(fqTypeName);
		}
		return classInfo;
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

	protected JavaClassInfo makeGenericClassInfo(JavaClassInfo classInfo, String genericTypeArguments)
	{
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
			JavaClassInfo typeArgumentCI = resolveClassInfo(typeArgumentsSplit[a]);
			typeArgumentCIs[a] = resolveClassInfo(typeArgumentsSplit[a]);
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
	public int incremetIndentationLevel()
	{
		indentationLevel++;
		return indentationLevel;
	}

	@Override
	public int decremetIndentationLevel()
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
		return typeOnStack;
	}

	@Override
	public void setTypeOnStack(String typeOnStack)
	{
		if ("<nulltype>".equals(typeOnStack))
		{
			typeOnStack = null;
		}
		this.typeOnStack = typeOnStack;
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
}
