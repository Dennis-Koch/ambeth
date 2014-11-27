package de.osthus.esmeralda.handler;

import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.esmeralda.handler.csharp.expr.IOwnerWriter;

public interface IMethodParameterProcessor
{
	void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod, IOwnerWriter ownerWriter);
}
