package de.osthus.esmeralda;

import java.io.File;

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

	private HashSet<TypeUsing> usedTypes;

	private IMap<String, String> imports;

	private IList<TypeUsing> usings;

	private Field field;

	private Method method;

	public File getTargetPath()
	{
		return targetPath;
	}

	public void setTargetPath(File targetPath)
	{
		this.targetPath = targetPath;
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
