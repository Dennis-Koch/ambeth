package de.osthus.esmeralda.handler.js;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.esmeralda.ConversionContext;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JsHelper implements IJsHelper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Writer newLineIntend(ConversionContext context, Writer writer) throws IOException
	{
		return null;
	}

	@Override
	public void scopeIntend(ConversionContext context, Writer writer, IBackgroundWorkerDelegate run) throws Throwable
	{
	}

	@Override
	public File createTargetFile(ConversionContext context)
	{
		return null;
	}

	@Override
	public Path createRelativeTargetPath(ConversionContext context)
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
	public Writer writeType(String typeName, ConversionContext context, Writer writer) throws Throwable
	{
		return null;
	}

	@Override
	public boolean newLineIntendIfFalse(boolean firstLine, ConversionContext context, Writer writer) throws Throwable
	{
		return false;
	}

	@Override
	public boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType, ConversionContext context) throws Throwable
	{
		return false;
	}

	@Override
	public boolean spaceIfFalse(boolean value, ConversionContext context, Writer writer) throws Throwable
	{
		return false;
	}

	@Override
	public boolean writeModifiers(BaseJavaClassModel javaClassModel, ConversionContext context, Writer writer) throws Throwable
	{
		return false;
	}

	@Override
	public Writer writeAnnotations(BaseJavaClassModel handle, ConversionContext context, Writer writer) throws Throwable
	{
		return null;
	}

	@Override
	public Writer writeAnnotation(Annotation annotation, ConversionContext context, Writer writer) throws Throwable
	{
		return null;
	}

	@Override
	public Writer writeNewInstance(JCNewClass initializer, ConversionContext context, Writer writer) throws Throwable
	{
		return null;
	}

	@Override
	public Writer writeGenericTypeArguments(List<Type> genericTypeArguments, ConversionContext context, Writer writer) throws Throwable
	{
		return null;
	}

	@Override
	public Writer writeMethodArguments(List<JCExpression> methodArguments, ConversionContext context, Writer writer) throws Throwable
	{
		return null;
	}
}
