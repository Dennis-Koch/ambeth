package de.osthus.esmeralda.handler.csharp.transformer;

import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.MethodKey;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class JavaLangReflectArrayTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICsHelper languageHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.reflect.Array.class, "newInstance", "System.Array", "CreateInstance", false, Class.class, int.class);

		mapCustomTransformation(java.lang.reflect.Array.class, "getLength", Object.class).setStatic(true).setParameterProcessor(new IMethodParameterProcessor()
		{
			@Override
			public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod,
					IOwnerWriter ownerWriter)
			{
				IWriter writer = context.getWriter();

				writer.append("((");
				languageHelper.writeTypeDirect("System.Array");
				writer.append(')');
				languageHelper.writeExpressionTree(methodInvocation.getArguments().get(0));
				writer.append(").Length");
			}
		});

		mapCustomTransformation(java.lang.reflect.Array.class, "get", Object.class, int.class).setStatic(true).setParameterProcessor(
				new IMethodParameterProcessor()
				{
					@Override
					public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod,
							IOwnerWriter ownerWriter)
					{
						IWriter writer = context.getWriter();

						writer.append("((");
						languageHelper.writeTypeDirect("System.Array");
						writer.append(')');
						languageHelper.writeExpressionTree(methodInvocation.getArguments().get(0));
						writer.append(").Get(");
						languageHelper.writeExpressionTree(methodInvocation.getArguments().get(1));
						writer.append(')');
					}
				});

		mapCustomTransformation(java.lang.reflect.Array.class, "set", Object.class, int.class, Object.class).setStatic(true).setParameterProcessor(
				new IMethodParameterProcessor()
				{
					@Override
					public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod,
							IOwnerWriter ownerWriter)
					{
						IWriter writer = context.getWriter();

						writer.append("((");
						languageHelper.writeTypeDirect("System.Array");
						writer.append(')');
						languageHelper.writeExpressionTree(methodInvocation.getArguments().get(0));
						writer.append(").Set(");
						languageHelper.writeExpressionTree(methodInvocation.getArguments().get(1));
						writer.append(", ");
						languageHelper.writeExpressionTree(methodInvocation.getArguments().get(2));
						writer.append(')');
					}
				});
	}

	@Override
	public ITransformedMethod buildMethodTransformation(MethodKey methodKey)
	{
		ITransformedMethod transformedMethod = super.buildMethodTransformation(methodKey);
		return transformedMethod;
	}
}
