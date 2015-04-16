package de.osthus.esmeralda;

import java.io.File;

import com.sun.source.tree.Tree;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.misc.IWriter;
import de.osthus.esmeralda.misc.StatementCount;
import de.osthus.esmeralda.snippet.ISnippetManager;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;
import demo.codeanalyzer.common.model.Method;

public interface IConversionContext
{
	IConversionContext getCurrent();

	ILanguageSpecific getLanguageSpecific();

	boolean isGenericTypeSupported();

	void setCurrent(IConversionContext current);

	String getLanguage();

	File getTargetPath();

	void setTargetPath(File targetPath);

	File getSnippetPath();

	String getPathPrefixRemove();

	IWriter getWriter();

	void setWriter(IWriter writer);

	ILanguageHelper getLanguageHelper();

	boolean isDryRun();

	void setDryRun(boolean dryRun);

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

	void setMethodIntern(Method method);

	ISnippetManager getSnippetManager();

	void setSnippetManager(ISnippetManager snippetManager);

	void addCalledMethod(ITransformedMethod method);

	void queuePostProcess(IPostProcess postProcess);

	IList<IPostProcess> getPostProcesses();

	String getTypeOnStack();

	void setTypeOnStack(String typeOnStack);

	StatementCount getMetric();

	void setMetric(StatementCount metric);

	void setSkipFirstBlockStatement(boolean skipFirstBlockStatement);

	boolean isSkipFirstBlockStatement();

	void mapSymbolTransformation(String sourceSymbol, String targetSymbol);

	String getTransformedSymbol(String sourceSymbol);

	void startWriteToStash();

	void endWriteToStash();

	Tree getCurrentTree();

	void setCurrentTree(Tree currentTree);

	Object pushTypeErasureHint(JavaClassInfo classInfo);

	void popTypeErasureHint(Object pushResult);

	JavaClassInfo lookupTypeErasureHint(String fqName);

	void pushVariableDeclBlock();

	void popVariableDeclBlock();

	void pushVariableDecl(String variableName, JavaClassInfo type);

	JavaClassInfo lookupVariableDecl(String variableName);
}