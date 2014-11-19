package de.osthus.esmeralda;

import java.io.File;
import java.util.List;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import demo.codeanalyzer.common.model.Annotation;
import demo.codeanalyzer.common.model.BaseJavaClassModel;

public interface ILanguageHelper
{
	void newLineIntend();

	boolean newLineIntendIfFalse(boolean firstLine);

	void scopeIntend(IBackgroundWorkerDelegate run);

	File createTargetFile();

	String camelCaseName(String typeName);

	boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType);

	boolean writeStringIfFalse(String value, boolean condition);

	void writeType(String typeName);

	boolean writeModifiers(BaseJavaClassModel javaClassModel);

	void writeAnnotations(BaseJavaClassModel handle);

	void writeAnnotation(Annotation annotation);

	void writeNewInstance(JCNewClass initializer);

	void writeGenericTypeArguments(List<Type> genericTypeArguments);

	void writeMethodArguments(List<JCExpression> methodArguments);
}
