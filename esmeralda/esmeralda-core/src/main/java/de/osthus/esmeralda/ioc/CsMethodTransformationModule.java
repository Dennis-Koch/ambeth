package de.osthus.esmeralda.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.handler.IFieldTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.IMethodTransformerExtension;
import de.osthus.esmeralda.handler.IMethodTransformerExtensionExtendable;
import de.osthus.esmeralda.handler.csharp.transformer.DefaultFieldTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.DefaultMethodParameterProcessor;
import de.osthus.esmeralda.handler.csharp.transformer.DefaultMethodTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaIoPrintstreamTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangCharacterTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangClassTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangIterableTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangObjectTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangRefReferenceTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangReflectArrayTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangReflectConstructorTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangReflectFieldTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangStringTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaLangThreadLocalTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilCollectionTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilEnumerationTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilIteratorTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilListTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.JavaUtilMapEntryTransformer;
import de.osthus.esmeralda.handler.csharp.transformer.StackTraceElementTransformer;
import de.osthus.esmeralda.handler.uni.transformer.AbstractMethodTransformerExtension;
import de.osthus.esmeralda.misc.Lang;

public class CsMethodTransformationModule implements IInitializingModule
{
	private static final String CsDefaultMethodTransformerName = Lang.C_SHARP + EsmeraldaCoreModule.DefaultMethodTransformerName;

	private static final String CsDefaultMethodParameterProcessor = EsmeraldaCoreModule.DefaultMethodParameterProcessor + Lang.C_SHARP;

	private static final String CsDefaultFieldTransformerName = Lang.C_SHARP + EsmeraldaCoreModule.DefaultFieldTransformerName;

	private IBeanConfiguration defaultMethodParameterProcessor;

	private IBeanConfiguration defaultMethodTransformer;

	private IBeanConfiguration defaultFieldTransformer;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		defaultMethodParameterProcessor = beanContextFactory.registerBean(CsDefaultMethodParameterProcessor, DefaultMethodParameterProcessor.class);

		defaultMethodTransformer = beanContextFactory.registerBean(DefaultMethodTransformer.class) //
				.propertyRef(AbstractMethodTransformerExtension.defaultMethodParameterProcessorProp, defaultMethodParameterProcessor);
		beanContextFactory.link(defaultMethodTransformer).to(IMethodTransformerExtensionExtendable.class).with(CsDefaultMethodTransformerName);

		defaultFieldTransformer = beanContextFactory.registerBean(DefaultFieldTransformer.class);
		beanContextFactory.link(defaultFieldTransformer).to(IFieldTransformerExtensionExtendable.class).with(CsDefaultFieldTransformerName);

		registerMethodTransformerExtension(beanContextFactory, JavaLangCharacterTransformer.class, java.lang.Character.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangClassTransformer.class, java.lang.Class.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangIterableTransformer.class, java.lang.Iterable.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangObjectTransformer.class, java.lang.Object.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangReflectArrayTransformer.class, java.lang.reflect.Array.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangReflectConstructorTransformer.class, java.lang.reflect.Constructor.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangReflectFieldTransformer.class, java.lang.reflect.Field.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangRefReferenceTransformer.class, java.lang.ref.Reference.class,
				java.lang.ref.WeakReference.class, java.lang.ref.SoftReference.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangStringTransformer.class, java.lang.String.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilCollectionTransformer.class, java.util.Collection.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilEnumerationTransformer.class, java.util.Enumeration.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilIteratorTransformer.class, java.util.Iterator.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilListTransformer.class, java.util.List.class);
		registerMethodTransformerExtension(beanContextFactory, JavaUtilMapEntryTransformer.class, java.util.Map.Entry.class);
		registerMethodTransformerExtension(beanContextFactory, JavaLangThreadLocalTransformer.class, java.lang.ThreadLocal.class);
		registerMethodTransformerExtension(beanContextFactory, JavaIoPrintstreamTransformer.class, java.io.PrintStream.class);
		registerMethodTransformerExtension(beanContextFactory, StackTraceElementTransformer.class, java.lang.StackTraceElement.class);
	}

	private IBeanConfiguration registerMethodTransformerExtension(IBeanContextFactory beanContextFactory,
			Class<? extends IMethodTransformerExtension> methodTransformerType, Class<?>... types)
	{
		IBeanConfiguration methodTransformer = beanContextFactory.registerBean(methodTransformerType)//
				.propertyRef(AbstractMethodTransformerExtension.defaultMethodParameterProcessorProp, defaultMethodParameterProcessor);
		for (Class<?> type : types)
		{
			beanContextFactory.link(methodTransformer).to(IMethodTransformerExtensionExtendable.class).with(Lang.C_SHARP + type.getName());
		}
		return methodTransformer;
	}
}
