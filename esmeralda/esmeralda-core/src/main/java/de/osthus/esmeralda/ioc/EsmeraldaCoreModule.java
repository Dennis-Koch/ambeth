package de.osthus.esmeralda.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.CodeProcessor;
import de.osthus.esmeralda.ConversionManager;
import de.osthus.esmeralda.handler.INodeHandlerExtendable;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.CsharpClassNodeHandler;
import de.osthus.esmeralda.handler.csharp.CsharpFieldNodeHandler;
import de.osthus.esmeralda.handler.csharp.CsharpHelper;
import de.osthus.esmeralda.handler.csharp.CsharpMethodNodeHandler;
import de.osthus.esmeralda.handler.csharp.ICsharpHelper;
import de.osthus.esmeralda.handler.js.IJsHelper;
import de.osthus.esmeralda.handler.js.JsClassNodeHandler;
import de.osthus.esmeralda.handler.js.JsHelper;
import de.osthus.esmeralda.misc.EsmeFileUtil;
import de.osthus.esmeralda.misc.EsmeType;
import de.osthus.esmeralda.misc.IEsmeFileUtil;
import de.osthus.esmeralda.misc.Lang;
import de.osthus.esmeralda.snippet.ISnippetManagerFactory;
import de.osthus.esmeralda.snippet.SnippetManagerFactory;

public class EsmeraldaCoreModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		// beanContextFactory.registerBean(InterfaceConverter.class);
		beanContextFactory.registerBean(ConversionManager.class);
		beanContextFactory.registerBean(CodeProcessor.class).autowireable(CodeProcessor.class);

		beanContextFactory.registerBean(SnippetManagerFactory.class).autowireable(ISnippetManagerFactory.class);

		beanContextFactory.registerBean(EsmeFileUtil.class).autowireable(IEsmeFileUtil.class);

		beanContextFactory.registerBean(CsharpHelper.class).autowireable(ICsharpHelper.class);
		beanContextFactory.registerBean(JsHelper.class).autowireable(IJsHelper.class);

		beanContextFactory.registerBean(ExtendableBean.class) //
				.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, INodeHandlerExtendable.class) //
				.propertyValue(ExtendableBean.P_PROVIDER_TYPE, INodeHandlerRegistry.class) //
				.autowireable(INodeHandlerExtendable.class, INodeHandlerRegistry.class);

		IBeanConfiguration csClassNodeHandler = beanContextFactory.registerBean(CsharpClassNodeHandler.class);
		beanContextFactory.link(csClassNodeHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.CLASS);
		IBeanConfiguration jsClassNodeHandler = beanContextFactory.registerBean(JsClassNodeHandler.class);
		beanContextFactory.link(jsClassNodeHandler).to(INodeHandlerExtendable.class).with(Lang.JS + EsmeType.CLASS);

		IBeanConfiguration csFieldNodeHandler = beanContextFactory.registerBean(CsharpFieldNodeHandler.class);
		beanContextFactory.link(csFieldNodeHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.FIELD);

		IBeanConfiguration csMethodNodeHandler = beanContextFactory.registerBean(CsharpMethodNodeHandler.class);
		beanContextFactory.link(csMethodNodeHandler).to(INodeHandlerExtendable.class).with(Lang.C_SHARP + EsmeType.METHOD);
	}
}
