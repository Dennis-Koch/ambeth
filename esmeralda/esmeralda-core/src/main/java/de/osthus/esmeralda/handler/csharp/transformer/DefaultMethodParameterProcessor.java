package de.osthus.esmeralda.handler.csharp.transformer;

import java.util.List;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.misc.IWriter;

public class DefaultMethodParameterProcessor implements IMethodParameterProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionContext context;

	@Autowired
	protected ICsHelper languageHelper;

	@Override
	public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod, IOwnerWriter ownerWriter)
	{
		IConversionContext context = this.context.getCurrent();
		IWriter writer = context.getWriter();
		List<JCExpression> arguments = methodInvocation.getArguments();

		if (owner != null)
		{
			ownerWriter.writeOwner(owner);
			writer.append('.');
		}
		String methodName = transformedMethod.getName();
		writer.append(methodName);

		if (!transformedMethod.isPropertyInvocation())
		{
			writer.append('(');
			for (int a = 0, size = arguments.size(); a < size; a++)
			{
				JCExpression arg = arguments.get(a);
				if (a > 0)
				{
					writer.append(", ");
				}
				languageHelper.writeExpressionTree(arg);
			}
			writer.append(')');
		}
		else if (arguments.size() == 1)
		{
			writer.append(" = ");
			JCExpression argument = arguments.get(0);
			languageHelper.writeExpressionTree(argument);
			writer.append(';');
		}
		else if (arguments.size() > 0)
		{
			// C# will be an assignment to a property (setter-semantics)
			throw new IllegalStateException("Property assignment not yet supported: " + methodInvocation);
			// writer.append(" = ");
			// boolean firstArgument = true;
			// for (JCExpression argument : methodInvocation.getArguments())
			// {
			// firstArgument = languageHelper.writeStringIfFalse(", ", firstArgument);
			// languageHelper.writeExpressionTree(argument);
			// }
			// writer.append(';');
		}
	}
}
