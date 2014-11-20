package de.osthus.esmeralda.ioc;

import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.esmeralda.CodeProcessor;
import de.osthus.esmeralda.ConversionManager;
import de.osthus.esmeralda.IConversionContext;
import de.osthus.esmeralda.handler.ConversionContextBean;
import de.osthus.esmeralda.handler.IExpressionHandler;
import de.osthus.esmeralda.handler.IExpressionHandlerExtendable;
import de.osthus.esmeralda.handler.INodeHandlerExtendable;
import de.osthus.esmeralda.handler.INodeHandlerRegistry;
import de.osthus.esmeralda.handler.csharp.BinaryExpressionHandler;
import de.osthus.esmeralda.handler.csharp.CsharpClassNodeHandler;
import de.osthus.esmeralda.handler.csharp.CsharpFieldNodeHandler;
import de.osthus.esmeralda.handler.csharp.CsharpHelper;
import de.osthus.esmeralda.handler.csharp.CsharpMethodNodeHandler;
import de.osthus.esmeralda.handler.csharp.FieldAccessExpressionHandler;
import de.osthus.esmeralda.handler.csharp.ICsharpHelper;
import de.osthus.esmeralda.handler.csharp.LiteralExpressionHandler;
import de.osthus.esmeralda.handler.csharp.MethodInvocationExpressionHandler;
import de.osthus.esmeralda.handler.csharp.NewArrayExpressionHandler;
import de.osthus.esmeralda.handler.csharp.NewClassExpressionHandler;
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
		beanContextFactory.registerBean(ConversionContextBean.class).autowireable(IConversionContext.class);

		beanContextFactory.registerBean(SnippetManagerFactory.class).autowireable(ISnippetManagerFactory.class);

		beanContextFactory.registerBean(EsmeFileUtil.class).autowireable(IEsmeFileUtil.class);

		beanContextFactory.registerBean(CsharpHelper.class).autowireable(ICsharpHelper.class, IExpressionHandlerExtendable.class);
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

		// expressions
		registerExpressionHandler(beanContextFactory, LiteralExpressionHandler.class, JCLiteral.class, JCIdent.class);
		registerExpressionHandler(beanContextFactory, BinaryExpressionHandler.class, JCBinary.class);
		registerExpressionHandler(beanContextFactory, FieldAccessExpressionHandler.class, JCFieldAccess.class);
		registerExpressionHandler(beanContextFactory, MethodInvocationExpressionHandler.class, JCMethodInvocation.class);
		registerExpressionHandler(beanContextFactory, NewArrayExpressionHandler.class, JCNewArray.class);
		registerExpressionHandler(beanContextFactory, NewClassExpressionHandler.class, JCNewClass.class);
	}

	protected void registerExpressionHandler(IBeanContextFactory beanContextFactory, Class<? extends IExpressionHandler> expressionHandlerType,
			Class<?>... expressionTypes)
	{
		IBeanConfiguration expressionHandler = beanContextFactory.registerBean(expressionHandlerType);
		for (Class<?> expressionType : expressionTypes)
		{
			beanContextFactory.link(expressionHandler).to(IExpressionHandlerExtendable.class).with(expressionType);
		}
	}
}
