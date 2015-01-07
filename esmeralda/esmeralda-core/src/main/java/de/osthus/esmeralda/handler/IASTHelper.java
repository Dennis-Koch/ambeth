package de.osthus.esmeralda.handler;

import java.util.List;

import com.sun.source.tree.TypeParameterTree;

import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import demo.codeanalyzer.common.model.BaseJavaClassModel;
import demo.codeanalyzer.common.model.Method;

public interface IASTHelper
{
	String extractNonGenericType(String typeName);

	boolean isAnnotatedWith(BaseJavaClassModel model, Class<?> annotationType);

	List<TypeParameterTree> resolveAllTypeParameters();

	String resolveTypeFromVariableName(String variableName);

	String resolveFqTypeFromTypeName(String typeName);

	boolean hasGenericTypeArguments(Method method);

	String[] splitTypeArgument(String typeArguments);

	String writeToStash(IBackgroundWorkerDelegate run);

	<R> R writeToStash(IResultingBackgroundWorkerDelegate<R> run);

	<R, A> R writeToStash(IResultingBackgroundWorkerParamDelegate<R, A> run, A arg);
}