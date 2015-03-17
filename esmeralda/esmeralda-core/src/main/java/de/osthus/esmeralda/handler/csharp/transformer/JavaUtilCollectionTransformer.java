package de.osthus.esmeralda.handler.csharp.transformer;

import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.ILanguageHelper;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.misc.IWriter;

public class JavaUtilCollectionTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.util.Collection.class, "size", "System.Collections.Generic.ICollection", "Count", true);
		mapTransformation(java.util.Collection.class, "iterator", "System.Collections.Generic.ICollection", "GetEnumerator", false);

		mapCustomTransformation(java.util.Collection.class, "toArray").setParameterProcessor(new IMethodParameterProcessor()
		{
			@Override
			public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod,
					IOwnerWriter ownerWriter)
			{
				ILanguageHelper languageHelper = context.getLanguageHelper();
				IWriter writer = context.getWriter();

				languageHelper.writeTypeDirect("De.Osthus.Ambeth.Util.ListUtil");
				writer.append(".AnyToArray(");
				ownerWriter.writeOwner(owner);
				writer.append(')');
			}
		});
	}
}
