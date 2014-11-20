package de.osthus.esmeralda;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ConversionContext implements IConversionContext
{
	public static final Pattern genericTypePattern = Pattern.compile("([^<>]+)<(.+)>");

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

	private boolean dryRun = false;

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

	@Override
	public JavaClassInfo resolveClassInfo(String fqTypeName)
	{
		JavaClassInfo classInfo = fqNameToClassInfoMap.get(fqTypeName);
		if (classInfo != null)
		{
			return classInfo;
		}
		if ("<none>".equals(fqTypeName))
		{
			return null;
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
			JavaClassInfo nonGenericClassInfo = resolveClassInfo(nonGenericType);
			return makeGenericClassInfo(nonGenericClassInfo, genericTypeArguments);
		}
		if (fqTypeName.contains(".repackaged."))
		{
			throw new SkipGenerationException();
		}
		throw new TypeResolveException(fqTypeName);
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
}
