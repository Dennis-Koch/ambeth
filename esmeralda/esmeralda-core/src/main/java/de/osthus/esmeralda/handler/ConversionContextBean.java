package de.osthus.esmeralda.handler;

import java.io.File;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.IWriter;
import de.osthus.esmeralda.TypeUsing;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public class ConversionContextBean implements IConversionContext, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ThreadLocal<IConversionContext> conversionContextTL = new ThreadLocal<IConversionContext>();

	@Override
	public void cleanupThreadLocal()
	{
		conversionContextTL.remove();
	}

	protected IConversionContext getContext()
	{
		return conversionContextTL.get();
	}

	@Override
	public IConversionContext getCurrent()
	{
		return getContext();
	}

	@Override
	public void setCurrent(IConversionContext current)
	{
		if (current == null)
		{
			conversionContextTL.remove();
		}
		else
		{
			conversionContextTL.set(current);
		}
	}

	@Override
	public File getTargetPath()
	{
		return getContext().getTargetPath();
	}

	@Override
	public void setTargetPath(File targetPath)
	{
		getContext().setTargetPath(targetPath);
	}

	@Override
	public void setFqNameToClassInfoMap(IMap<String, JavaClassInfo> fqNameToClassInfoMap)
	{
		getContext().setFqNameToClassInfoMap(fqNameToClassInfoMap);
	}

	@Override
	public IMap<String, JavaClassInfo> getFqNameToClassInfoMap()
	{
		return getContext().getFqNameToClassInfoMap();
	}

	@Override
	public IWriter getWriter()
	{
		return getContext().getWriter();
	}

	@Override
	public void setWriter(IWriter writer)
	{
		getContext().setWriter(writer);
	}

	@Override
	public JavaClassInfo resolveClassInfo(String fqTypeName)
	{
		return getContext().resolveClassInfo(fqTypeName);
	}

	@Override
	public void setTargetFile(File targetFile)
	{
		getContext().setTargetFile(targetFile);
	}

	@Override
	public String getLanguagePath()
	{
		return getContext().getLanguagePath();
	}

	@Override
	public void setLanguagePath(String languagePath)
	{
		getContext().setLanguagePath(languagePath);
	}

	@Override
	public String getNsPrefixAdd()
	{
		return getContext().getNsPrefixAdd();
	}

	@Override
	public void setNsPrefixAdd(String nsPrefixAdd)
	{
		getContext().setNsPrefixAdd(nsPrefixAdd);
	}

	@Override
	public String getNsPrefixRemove()
	{
		return getContext().getNsPrefixRemove();
	}

	@Override
	public void setNsPrefixRemove(String nsPrefixRemove)
	{
		getContext().setNsPrefixRemove(nsPrefixRemove);
	}

	@Override
	public int getIndentationLevel()
	{
		return getContext().getIndentationLevel();
	}

	@Override
	public void setIndentationLevel(int indentationLevel)
	{
		getContext().setIndentationLevel(indentationLevel);
	}

	@Override
	public int incremetIndentationLevel()
	{
		return getContext().incremetIndentationLevel();
	}

	@Override
	public int decremetIndentationLevel()
	{
		return getContext().decremetIndentationLevel();
	}

	@Override
	public JavaClassInfo getClassInfo()
	{
		return getContext().getClassInfo();
	}

	@Override
	public void setClassInfo(JavaClassInfo classInfo)
	{
		getContext().setClassInfo(classInfo);
	}

	@Override
	public HashSet<TypeUsing> getUsedTypes()
	{
		return getContext().getUsedTypes();
	}

	@Override
	public void setUsedTypes(HashSet<TypeUsing> usedTypes)
	{
		getContext().setUsedTypes(usedTypes);
	}

	@Override
	public IMap<String, String> getImports()
	{
		return getContext().getImports();
	}

	@Override
	public void setImports(IMap<String, String> imports)
	{
		getContext().setImports(imports);
	}

	@Override
	public IList<TypeUsing> getUsings()
	{
		return getContext().getUsings();
	}

	@Override
	public void setUsings(IList<TypeUsing> usings)
	{
		getContext().setUsings(usings);
	}

	@Override
	public Field getField()
	{
		return getContext().getField();
	}

	@Override
	public void setField(Field field)
	{
		getContext().setField(field);
	}

	@Override
	public Method getMethod()
	{
		return getContext().getMethod();
	}

	@Override
	public void setMethod(Method method)
	{
		getContext().setMethod(method);
	}

	@Override
	public String toString()
	{
		IConversionContext context = getContext();
		if (context == null)
		{
			return super.toString();
		}
		return context.toString();
	}
}
