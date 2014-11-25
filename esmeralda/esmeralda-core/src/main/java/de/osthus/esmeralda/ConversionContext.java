package de.osthus.esmeralda;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import de.osthus.esmeralda.handler.IClassInfoFactory;
import de.osthus.esmeralda.handler.csharp.expr.NewClassExpressionHandler;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ConversionContext implements IConversionContext
{
	public static final Pattern genericTypePattern = Pattern.compile("\\.?([^<>]+)<(.+)>");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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

	private ISnippetManager snippetManager;

	private final ArrayList<IPostProcess> postProcesses = new ArrayList<IPostProcess>();

	private boolean dryRun = false;

	private String typeOnStack;

	private StatementCount metric;

	protected IClassInfoFactory classInfoFactory;

	public void setClassInfoFactory(IClassInfoFactory classInfoFactory)
	{
		this.classInfoFactory = classInfoFactory;
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
		Matcher genericTypeMatcher = genericTypePattern.matcher(fqTypeName);
		if (genericTypeMatcher.matches())
		{
			String nonGenericType = genericTypeMatcher.group(1);
			String genericTypeArguments = genericTypeMatcher.group(2);
			JavaClassInfo nonGenericClassInfo = resolveClassInfo(nonGenericType, tryOnly);
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

	protected JavaClassInfo makeGenericClassInfo(JavaClassInfo classInfo, String genericTypeArguments)
	{
		// TODO: create new instance of javaClassInfo and replace the corresponding generic type parameters according to the given generic type arguments.
		return classInfo;
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
}
