package de.osthus.esmeralda.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.handler.IFieldTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.js.transformer.DefaultMethodParameterProcessor;
import de.osthus.esmeralda.handler.js.transformer.DefaultMethodTransformer;
import de.osthus.esmeralda.handler.js.transformer.JavaIoPrintstreamTransformer;
import de.osthus.esmeralda.handler.js.transformer.JavaLangCharSequenceTransformer;
import de.osthus.esmeralda.handler.js.transformer.JavaLangObjectTransformer;
import de.osthus.esmeralda.handler.js.transformer.JavaLangStringTransformer;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.handler.uni.transformer.DefaultFieldTransformer;
import de.osthus.esmeralda.misc.Lang;

public class JsMethodTransformationModule implements IInitializingModule
{
	private static final String JsDefaultMethodTransformerName = Lang.JS + EsmeraldaCoreModule.DefaultMethodTransformerName;

	private static final String JsDefaultMethodParameterProcessor = EsmeraldaCoreModule.DefaultMethodParameterProcessor + Lang.JS;

	private static final String JsDefaultFieldTransformerName = Lang.JS + EsmeraldaCoreModule.DefaultFieldTransformerName;

	private IBeanConfiguration defaultMethodParameterProcessor;

	private IBeanConfiguration defaultMethodTransformer;

	private IBeanConfiguration defaultFieldTransformer;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		defaultMethodParameterProcessor = beanContextFactory.registerBean(JsDefaultMethodParameterProcessor, DefaultMethodParameterProcessor.class);

		defaultMethodTransformer = beanContextFactory.registerBean(DefaultMethodTransformer.class) //
				.propertyRef(AbstractMethodTransformerExtension.defaultMethodParameterProcessorProp, defaultMethodParameterProcessor);
		beanContextFactory.link(defaultMethodTransformer).to(IMethodTransformerExtensionExtendable.class).with(JsDefaultMethodTransformerName);

		defaultFieldTransformer = beanContextFactory.registerBean(DefaultFieldTransformer.class);
		beanContextFactory.link(defaultFieldTransformer).to(IFieldTransformerExtensionExtendable.class).with(JsDefaultFieldTransformerName);

		registerMethodTransformerExtension(beanContextFactory, JavaIoPrintstreamTransformer.class, java.io.PrintStream.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangCharSequenceTransformer.class, java.lang.CharSequence.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangObjectTransformer.class, java.lang.Object.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangStringTransformer.class, java.lang.String.class);
	}

	private IBeanConfiguration registerMethodTransformerExtension(IBeanContextFactory beanContextFactory,
			Class<? extends IMethodTransformerExtension> methodTransformerType, Class<?> type)
	{
		IBeanConfiguration methodTransformer = beanContextFactory.registerBean(methodTransformerType) //
				.propertyRef(AbstractMethodTransformerExtension.defaultMethodParameterProcessorProp, defaultMethodParameterProcessor);
		beanContextFactory.link(methodTransformer).to(IMethodTransformerExtensionExtendable.class).with(Lang.JS + type.getName());
		return methodTransformer;
	}
}
