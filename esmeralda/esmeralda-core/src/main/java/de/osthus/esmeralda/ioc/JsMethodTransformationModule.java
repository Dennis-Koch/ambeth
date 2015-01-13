package de.osthus.esmeralda.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.js.IJsMethodTransformer;
import de.osthus.esmeralda.handler.js.MethodTransformer;
import de.osthus.esmeralda.handler.js.transformer.DefaultMethodParameterProcessor;
import de.osthus.esmeralda.handler.js.transformer.DefaultMethodTransformer;
import de.osthus.esmeralda.handler.js.transformer.JavaIoPrintstreamTransformer;
import de.osthus.esmeralda.handler.js.transformer.JavaLangCharSequenceTransformer;
import de.osthus.esmeralda.handler.js.transformer.JavaLangStringTransformer;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.misc.Lang;

public class JsMethodTransformationModule implements IInitializingModule
{
	public static final String DefaultMethodTransformerName = "defaultJsMethodTransformer";

	public static final String DefaultMethodParameterProcessor = "defaultJsMethodParameterProcessor";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration defaultMethodParameterProcessor = beanContextFactory.registerBean(DefaultMethodParameterProcessor,
				DefaultMethodParameterProcessor.class);

		IBeanConfiguration defaultMethodTransformer = beanContextFactory.registerBean(DefaultMethodTransformerName, DefaultMethodTransformer.class)//
				.propertyRef(defaultMethodParameterProcessor)//
				.ignoreProperties(AbstractMethodTransformerExtension.defaultMethodTransformerExtensionProp);

		beanContextFactory.registerBean(MethodTransformer.class)//
				.propertyRef(defaultMethodTransformer)//
				.autowireable(IJsMethodTransformer.class);

		registerMethodTransformerExtension(beanContextFactory, JavaIoPrintstreamTransformer.class, java.io.PrintStream.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangStringTransformer.class, java.lang.String.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangCharSequenceTransformer.class, java.lang.CharSequence.class);
	}

	protected static IBeanConfiguration registerMethodTransformerExtension(IBeanContextFactory beanContextFactory,
			Class<? extends IMethodTransformerExtension> methodTransformerType, Class<?> type)
	{
		IBeanConfiguration methodTransformer = beanContextFactory.registerBean(methodTransformerType)//
				.propertyRefs(DefaultMethodTransformerName, DefaultMethodParameterProcessor);
		beanContextFactory.link(methodTransformer).to(IMethodTransformerExtensionExtendable.class).with(Lang.JS + type.getName());
		return methodTransformer;
	}
}
