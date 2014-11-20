package de.osthus.esmeralda;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.JavaClassInfo;

public interface ILanguageHelper
{
	void newLineIntend();

	boolean newLineIntendIfFalse(boolean firstLine);

	void scopeIntend(IBackgroundWorkerDelegate run);

	File createTargetFile();

	Path createRelativeTargetPath();

	String createTargetFileName(JavaClassInfo classInfo);

	String camelCaseName(String typeName);

	boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType);

	boolean writeStringIfFalse(String value, boolean condition);

	void writeType(String typeName);

	void writeMethodArguments(JCExpression methodInvocation);

	boolean writeModifiers(BaseJavaClassModel javaClassModel);

	void writeAnnotations(BaseJavaClassModel handle);

	void writeAnnotation(Annotation annotation);

	void writeNewInstance(JCNewClass initializer);

	void writeExpressionTree(ExpressionTree expressionTree);

	void writeGenericTypeArguments(List<Type> genericTypeArguments);

	void writeMethodArguments(List<JCExpression> methodArguments);
}
