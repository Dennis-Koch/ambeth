package de.osthus.esmeralda.handler.js;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JsHelper implements IJsHelper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void newLineIntend()
	{
	}

	@Override
	public boolean newLineIntendIfFalse(boolean firstLine)
	{
		return false;
	}

	@Override
	public void scopeIntend(IBackgroundWorkerDelegate run)
	{
	}

	@Override
	public File createTargetFile()
	{
		return null;
	}

	@Override
	public Path createRelativeTargetPath()
	{
		return null;
	}

	@Override
	public String createTargetFileName(JavaClassInfo classInfo)
	{
		return null;
	}

	@Override
	public String camelCaseName(String typeName)
	{
		return null;
	}

	@Override
	public boolean writeStringIfFalse(String value, boolean condition)
	{
		return false;
	}

	@Override
	public void writeType(String typeName)
	{
	}

	@Override
	public boolean writeModifiers(BaseJavaClassModel javaClassModel)
	{
		return false;
	}

	@Override
	public void writeAnnotations(BaseJavaClassModel handle)
	{
	}

	@Override
	public void writeAnnotation(Annotation annotation)
	{
	}

	@Override
	public void writeGenericTypeArguments(List<Type> genericTypeArguments)
	{
	}

	@Override
	public void writeMethodArguments(List<JCExpression> methodArguments)
	{
	}

	@Override
	public void writeMethodArguments(JCExpression methodInvocation)
	{
	}

	@Override
	public void writeExpressionTree(Tree expressionTree)
	{
	}

	@Override
	public void writeTypeDirect(String typeName)
	{
	}

	@Override
	public String writeToStash(IBackgroundWorkerDelegate run)
	{
		return null;
	}

	@Override
	public <R, A> R writeToStash(IResultingBackgroundWorkerParamDelegate<R, A> run, A arg)
	{
		return null;
	}

}
