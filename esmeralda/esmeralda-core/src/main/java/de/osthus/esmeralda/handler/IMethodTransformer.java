package de.osthus.esmeralda.handler;

import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCExpression;

import demo.codeanalyzer.common.model.Method;

public interface IMethodTransformer
{
	ITransformedMethod transform(String owner, String methodName, List<JCExpression> parameterTypes);

	ITransformedMemberAccess transformFieldAccess(String owner, String name);

	ITransformedMethod transformMethodDeclaration(Method method);
}
