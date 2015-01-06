package de.osthus.esmeralda;

import java.io.File;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public interface IConversionContext
{
	IConversionContext getCurrent();

	boolean isGenericTypeSupported();

	void setCurrent(IConversionContext current);

	String getLanguage();

	File getTargetPath();

	void setTargetPath(File targetPath);

	File getSnippetPath();

	void setFqNameToClassInfoMap(IMap<String, JavaClassInfo> fqNameToClassInfoMap);

	IMap<String, JavaClassInfo> getFqNameToClassInfoMap();

	IWriter getWriter();

	void setWriter(IWriter writer);

	ILanguageHelper getLanguageHelper();

	boolean isDryRun();

	void setDryRun(boolean dryRun);

	JavaClassInfo resolveClassInfo(String fqTypeName);

	JavaClassInfo resolveClassInfo(String fqTypeName, boolean tryOnly);

	void setTargetFile(File targetFile);

	String getLanguagePath();

	void setLanguagePath(String languagePath);

	String getNsPrefixAdd();

	void setNsPrefixAdd(String nsPrefixAdd);

	String getNsPrefixRemove();

	void setNsPrefixRemove(String nsPrefixRemove);

	int getIndentationLevel();

	void setIndentationLevel(int indentationLevel);

	int incrementIndentationLevel();

	int decrementIndentationLevel();

	JavaClassInfo getClassInfo();

	void setClassInfo(JavaClassInfo classInfo);

	HashSet<TypeUsing> getUsedTypes();

	void setUsedTypes(HashSet<TypeUsing> usedTypes);

	IMap<String, String> getImports();

	void setImports(IMap<String, String> imports);

	IList<TypeUsing> getUsings();

	void setUsings(IList<TypeUsing> usings);

	Field getField();

	void setField(Field field);

	Method getMethod();

	void setMethod(Method method);

	ISnippetManager getSnippetManager();

	void setSnippetManager(ISnippetManager snippetManager);

	void queuePostProcess(IPostProcess postProcess);

	IList<IPostProcess> getPostProcesses();

	String getTypeOnStack();

	void setTypeOnStack(String typeOnStack);

	StatementCount getMetric();

	void setMetric(StatementCount metric);

	void setSkipFirstBlockStatement(boolean skipFirstBlockStatement);

	boolean isSkipFirstBlockStatement();
}