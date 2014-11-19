package de.osthus.esmeralda;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ConversionContext
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

	private final IMap<String, JavaClassInfo> fqNameToClassInfoMap;

	private HashSet<TypeUsing> usedTypes;

	private IMap<String, String> imports;

	private IList<TypeUsing> usings;

	private Field field;

	private Method method;

	public ConversionContext(IMap<String, JavaClassInfo> fqNameToClassInfoMap)
	{
		this.fqNameToClassInfoMap = fqNameToClassInfoMap;
	}

	public File getTargetPath()
	{
		return targetPath;
	}

	public void setTargetPath(File targetPath)
	{
		this.targetPath = targetPath;
	}

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
		if (fqTypeName.contains(".repackaged."))
		{
			throw new SkipGenerationException();
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
		throw new IllegalArgumentException("Could not resolve '" + fqTypeName + "'");
	}

	protected JavaClassInfo makeGenericClassInfo(JavaClassInfo classInfo, String genericTypeArguments)
	{
		// TODO: create new instance of javaClassInfo and replace the corresponding generic type parameters according to the given generic type arguments.
		return classInfo;
	}

	public void setTargetFile(File targetFile)
	{
		targetPath = targetPath;
	}

	public String getLanguagePath()
	{
		return languagePath;
	}

	public void setLanguagePath(String languagePath)
	{
		this.languagePath = languagePath;
	}

	public File getSnippetPath()
	{
		return snippetPath;
	}

	public void setSnippetPath(File snippetPath)
	{
		this.snippetPath = snippetPath;
	}

	public String getNsPrefixAdd()
	{
		return nsPrefixAdd;
	}

	public void setNsPrefixAdd(String nsPrefixAdd)
	{
		this.nsPrefixAdd = nsPrefixAdd;
	}

	public String getNsPrefixRemove()
	{
		return nsPrefixRemove;
	}

	public void setNsPrefixRemove(String nsPrefixRemove)
	{
		this.nsPrefixRemove = nsPrefixRemove;
	}

	public int getIndentationLevel()
	{
		return indentationLevel;
	}

	public void setIndentationLevel(int indentationLevel)
	{
		this.indentationLevel = indentationLevel;
	}

	public int incremetIndentationLevel()
	{
		indentationLevel++;
		return indentationLevel;
	}

	public int decremetIndentationLevel()
	{
		indentationLevel--;
		return indentationLevel;
	}

	public JavaClassInfo getClassInfo()
	{
		return classInfo;
	}

	public void setClassInfo(JavaClassInfo classInfo)
	{
		this.classInfo = classInfo;
	}

	public HashSet<TypeUsing> getUsedTypes()
	{
		return usedTypes;
	}

	public void setUsedTypes(HashSet<TypeUsing> usedTypes)
	{
		this.usedTypes = usedTypes;
	}

	public IMap<String, String> getImports()
	{
		return imports;
	}

	public void setImports(IMap<String, String> imports)
	{
		this.imports = imports;
	}

	public IList<TypeUsing> getUsings()
	{
		return usings;
	}

	public void setUsings(IList<TypeUsing> usings)
	{
		this.usings = usings;
	}

	public Field getField()
	{
		return field;
	}

	public void setField(Field field)
	{
		this.field = field;
	}

	public Method getMethod()
	{
		return method;
	}

	public void setMethod(Method method)
	{
		this.method = method;
	}
}
