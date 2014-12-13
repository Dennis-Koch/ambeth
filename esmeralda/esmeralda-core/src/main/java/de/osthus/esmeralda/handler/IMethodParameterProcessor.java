package de.osthus.esmeralda.handler;

import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

public interface IMethodParameterProcessor
{
	void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod, IOwnerWriter ownerWriter);
}
