package de.osthus.esmeralda.handler;

import java.util.List;

import com.sun.source.tree.TypeParameterTree;

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
}