package de.osthus.esmeralda.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.csharp.transformer.DefaultMethodParameterProcessor;
import de.osthus.esmeralda.handler.csharp.transformer.DefaultMethodTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaIoPrintstreamTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangClassTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangObjectTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangReflectFieldTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilListTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.StackTraceElementTransformer;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.misc.Lang;

public class CsMethodTransformationModule implements IInitializingModule
{
	private static final String CsDefaultMethodTransformerName = Lang.C_SHARP + EsmeraldaCoreModule.DefaultMethodTransformerName;

	private static final String CsDefaultMethodParameterProcessor = EsmeraldaCoreModule.DefaultMethodParameterProcessor + Lang.C_SHARP;

	private IBeanConfiguration defaultMethodParameterProcessor;

	private IBeanConfiguration defaultMethodTransformer;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		defaultMethodParameterProcessor = beanContextFactory.registerBean(CsDefaultMethodParameterProcessor, DefaultMethodParameterProcessor.class);

		defaultMethodTransformer = beanContextFactory.registerBean(DefaultMethodTransformer.class) //
				.propertyRef(AbstractMethodTransformerExtension.defaultMethodParameterProcessorProp, defaultMethodParameterProcessor);
		beanContextFactory.link(defaultMethodTransformer).to(IMethodTransformerExtensionExtendable.class).with(CsDefaultMethodTransformerName);

		registerMethodTransformerExtension(beanContextFactory, JavaLangClassTransformer.class, java.lang.Class.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangObjectTransformer.class, java.lang.Object.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangReflectFieldTransformer.class, java.lang.reflect.Field.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilListTransformer.class, java.util.List.class);
		registerMethodTransformerExtension(beanContextFactory, JavaIoPrintstreamTransformer.class, java.io.PrintStream.class);
		registerMethodTransformerExtension(beanContextFactory, StackTraceElementTransformer.class, java.lang.StackTraceElement.class);
	}

	private IBeanConfiguration registerMethodTransformerExtension(IBeanContextFactory beanContextFactory,
			Class<? extends IMethodTransformerExtension> methodTransformerType, Class<?> type)
	{
		IBeanConfiguration methodTransformer = beanContextFactory.registerBean(methodTransformerType)//
				.propertyRef(AbstractMethodTransformerExtension.defaultMethodTransformerExtensionProp, defaultMethodTransformer) //
				.propertyRef(AbstractMethodTransformerExtension.defaultMethodParameterProcessorProp, defaultMethodParameterProcessor);
		beanContextFactory.link(methodTransformer).to(IMethodTransformerExtensionExtendable.class).with(Lang.C_SHARP + type.getName());
		return methodTransformer;
	}
}
