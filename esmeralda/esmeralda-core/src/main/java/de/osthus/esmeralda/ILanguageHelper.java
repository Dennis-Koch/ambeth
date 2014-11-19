package de.osthus.esmeralda;

import java.io.File;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.JavaClassInfo;

public interface ILanguageHelper
{
	Writer newLineIntend(ConversionContext context, Writer writer) throws Throwable;

	boolean newLineIntendIfFalse(boolean firstLine, ConversionContext context, Writer writer) throws Throwable;

	void scopeIntend(ConversionContext context, Writer writer, IBackgroundWorkerDelegate run) throws Throwable;

	File createTargetFile(ConversionContext context);

	Path createRelativeTargetPath(ConversionContext context);

	String createTargetFileName(JavaClassInfo classInfo);

	String createNameSpace(ConversionContext context);

	String camelCaseName(String typeName);

	boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType, ConversionContext context) throws Throwable;

	boolean writeStringIfFalse(String value, boolean condition, ConversionContext context, Writer writer) throws Throwable;

	Writer writeType(String typeName, ConversionContext context, Writer writer) throws Throwable;

	boolean writeModifiers(BaseJavaClassModel javaClassModel, ConversionContext context, Writer writer) throws Throwable;

	Writer writeAnnotations(BaseJavaClassModel handle, ConversionContext context, Writer writer) throws Throwable;

	Writer writeAnnotation(Annotation annotation, ConversionContext context, Writer writer) throws Throwable;

	Writer writeNewInstance(JCNewClass initializer, ConversionContext context, Writer writer) throws Throwable;

	Writer writeGenericTypeArguments(List<Type> genericTypeArguments, ConversionContext context, Writer writer) throws Throwable;

	Writer writeMethodArguments(List<JCExpression> methodArguments, ConversionContext context, Writer writer) throws Throwable;
}
