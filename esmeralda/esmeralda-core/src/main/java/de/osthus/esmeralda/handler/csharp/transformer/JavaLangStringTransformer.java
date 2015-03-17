package de.osthus.esmeralda.handler.csharp.transformer;

import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.csharp.ICsHelper;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class JavaLangStringTransformer extends AbstractMethodTransformerExtension
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

		mapTransformation(String.class, "length", "System.String", "Length", true);
		mapTransformation(String.class, "charAt", "System.String", "idx[]", false, int.class).setIndexedInvocation(true);

		mapCustomTransformation(String.class, "isEmpty").setParameterProcessor(new IMethodParameterProcessor()
		{
			@Override
			public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod,
					IOwnerWriter ownerWriter)
			{
				IWriter writer = context.getWriter();

				ownerWriter.writeOwner(owner);
				writer.append(".Length == 0");
			}
		});
	}
}
