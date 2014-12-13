package de.osthus.esmeralda.handler.csharp.transformer;

import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.misc.IWriter;

public class JavaLangClassTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.Class.class, "getSimpleName", "System.Type", "Name", true);
		mapTransformation(java.lang.Class.class, "getName", "System.Type", "FullName", true);
		mapTransformation(java.lang.Class.class, "isPrimitive", "System.Type", "IsPrimitive", true);
		mapTransformation(java.lang.Class.class, "isInterface", "System.Type", "IsInterface", true);

		mapTransformation(java.lang.Class.class, "newInstance", "System.Activator", "CreateInstance", new IMethodParameterProcessor()
		{
			@Override
			public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod,
					IOwnerWriter ownerWriter)
			{
				IConversionContext context = JavaLangClassTransformer.this.context.getCurrent();
				IWriter writer = context.getWriter();

				languageHelper.writeTypeDirect(transformedMethod.getOwner());
				writer.append('.');
				writer.append(transformedMethod.getName());

				writer.append('(');
				ownerWriter.writeOwner(owner);
				writer.append(')');
			}
		});
	}
}
