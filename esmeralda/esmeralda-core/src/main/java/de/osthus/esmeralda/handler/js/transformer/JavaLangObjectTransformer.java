package de.osthus.esmeralda.handler.js.transformer;

import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.IMethodParameterProcessor;
import de.osthus.esmeralda.handler.IOwnerWriter;
import de.osthus.esmeralda.handler.ITransformedMethod;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.misc.IWriter;
import demo.codeanalyzer.common.model.Field;
import demo.codeanalyzer.common.model.JavaClassInfo;

public class JavaLangObjectTransformer extends AbstractMethodTransformerExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IJsHelper languageHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		mapTransformation(java.lang.Object.class, "getClass", "Ambeth", "getClass", new IMethodParameterProcessor()
		{
			@Override
			public void processMethodParameters(JCMethodInvocation methodInvocation, String owner, ITransformedMethod transformedMethod,
					IOwnerWriter ownerWriter)
			{
				IConversionContext context = JavaLangObjectTransformer.this.context.getCurrent();
				IWriter writer = context.getWriter();

				languageHelper.writeTypeDirect(transformedMethod.getOwner());
				writer.append('.');
				writer.append(transformedMethod.getName());

				if (owner != null)
				{
					HashSet<String> methodScopeVars = languageHelper.getLanguageSpecific().getMethodScopeVars();
					if (!methodScopeVars.contains(owner))
					{
						JavaClassInfo currentClass = context.getClassInfo();
						Field field = languageHelper.findFieldInHierarchy(owner, currentClass);
						if (field != null)
						{
							owner = "this." + owner;
						}
					}
				}
				else
				{
					owner = "this";
				}

				writer.append('(');
				ownerWriter.writeOwner(owner);
				writer.append(')');
			}
		});
	}
}
