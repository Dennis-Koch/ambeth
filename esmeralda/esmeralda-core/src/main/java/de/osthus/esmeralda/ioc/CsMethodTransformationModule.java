package de.osthus.esmeralda.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.csharp.transformer.JavaIoPrintstreamTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangClassTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangObjectTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangReflectFieldTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilListTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.StackTraceElementTransformer;
import de.osthus.esmeralda.misc.Lang;

public class CsMethodTransformationModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		registerMethodTransformerExtension(beanContextFactory, JavaLangClassTransformer.class, java.lang.Class.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangObjectTransformer.class, java.lang.Object.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangReflectFieldTransformer.class, java.lang.reflect.Field.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilListTransformer.class, java.util.List.class);
		registerMethodTransformerExtension(beanContextFactory, JavaIoPrintstreamTransformer.class, java.io.PrintStream.class);
		registerMethodTransformerExtension(beanContextFactory, StackTraceElementTransformer.class, java.lang.StackTraceElement.class);
	}

	protected static IBeanConfiguration registerMethodTransformerExtension(IBeanContextFactory beanContextFactory,
			Class<? extends IMethodTransformerExtension> methodTransformerType, Class<?> type)
	{
		IBeanConfiguration methodTransformer = beanContextFactory.registerBean(methodTransformerType)//
				.propertyRefs(EsmeraldaCoreModule.DefaultMethodTransformerName, EsmeraldaCoreModule.DefaultMethodParameterProcessor);
		beanContextFactory.link(methodTransformer).to(IMethodTransformerExtensionExtendable.class).with(Lang.C_SHARP + type.getName());
		return methodTransformer;
	}
}
