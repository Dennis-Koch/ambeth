package de.osthus.esmeralda;

import java.io.Writer;
import java.util.List;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;

public interface ILanguageHelper
{
	Writer newLineIntend(ConversionContext context, Writer writer) throws Throwable;

	boolean newLineIntendIfFalse(boolean firstLine, ConversionContext context, Writer writer) throws Throwable;

	void scopeIntend(ConversionContext context, Writer writer, IBackgroundWorkerDelegate run) throws Throwable;

	String camelCaseName(String typeName);

	boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType, ConversionContext context) throws Throwable;

	boolean spaceIfFalse(boolean value, ConversionContext context, Writer writer) throws Throwable;

	Writer writeType(String typeName, ConversionContext context, Writer writer) throws Throwable;

	boolean writeModifiers(BaseJavaClassModel javaClassModel, ConversionContext context, Writer writer) throws Throwable;

	Writer writeAnnotations(BaseJavaClassModel handle, ConversionContext context, Writer writer) throws Throwable;

	Writer writeAnnotation(Annotation annotation, ConversionContext context, Writer writer) throws Throwable;

	Writer writeNewInstance(JCNewClass initializer, ConversionContext context, Writer writer) throws Throwable;

	Writer writeGenericTypeArguments(List<Type> genericTypeArguments, ConversionContext context, Writer writer) throws Throwable;

	Writer writeMethodArguments(List<JCExpression> methodArguments, ConversionContext context, Writer writer) throws Throwable;

}
